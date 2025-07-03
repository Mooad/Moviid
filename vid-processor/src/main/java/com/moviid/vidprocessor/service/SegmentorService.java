package com.moviid.vidprocessor.service;

import com.moviid.bean.FileSegment;
import com.moviid.bean.GlobalVideoData;
import com.moviid.bean.sqs.SqsVideoSegmentMessage;
import com.moviid.proxy.sqs.SQSMessageLoader;
import com.moviid.utils.ProcessingStatus;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Component
public class SegmentorService {

    /* ----- longueur de segment adaptative (µs) ----- */
    private static final long US  = 1_000_000L;
    private static final long MIN = 60 * US;

    private static long pickSegmentLen(long totalUs) {
        long min = totalUs / MIN;
        if (min < 5)   return MIN;          // < 5 min  → 1 min
        if (min > 30)  return 5 * MIN;      // > 30 min → 5 min
        return 2 * MIN;                     // sinon    → 2 min
    }

    static { avutil.av_log_set_level(avutil.AV_LOG_ERROR); }

    @Autowired private ProcessorService processorService;
    @Autowired private SQSMessageLoader sqsLoader;

    public GlobalVideoData splitVideo(GlobalVideoData gv, ExecutorService pool) {

        String src = gv.getVideoFile().getPath();
        List<FileSegment> list = new ArrayList<>();

        String folder = "segment_" + Instant.now().toEpochMilli();
        String jobId  = String.valueOf(Instant.now().toEpochMilli());
        gv.setJobId(jobId);                                     // setter existant

        try (FFmpegFrameGrabber g = new FFmpegFrameGrabber(src)) {
            g.start();

            long totalUs = g.getLengthInTime();
            long segUs   = pickSegmentLen(totalUs);
            int  parts   = (int) Math.ceil((double) totalUs / segUs);

            CountDownLatch latch = new CountDownLatch(parts);

            for (int i = 0; i < parts; i++) {

                long st = i * segUs;
                long et = Math.min((i + 1L) * segUs, totalUs);

                String segId = "segment_" + i;
                String s3    = "s3://moviid/Segmentation/" + folder + "/" + segId + ".mp4";

                FileSegment fs = new FileSegment(
                        segId,                        // id
                        src,                          // local path
                        i,                            // order
                        st, et,                       // timestamps
                        folder,                       // parent folder
                        i,                            // segmentNumber
                        ProcessingStatus.WAITING_FOR_SEGMENTATION.name(),
                        jobId,
                        gv.getVideoSourceUrl(),
                        s3
                );
                list.add(fs);

                pool.submit(() -> {
                    try {
                        processorService.processSegment(fs, folder);
                        fs.setStatus(ProcessingStatus.WAITING_FOR_RECOGNITION.name());
                    } catch (Exception ex) {
                        fs.setStatus(ProcessingStatus.FAILED.name());
                        ex.printStackTrace();
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();                                     // bloque

            list.stream()
                    .filter(s -> ProcessingStatus.WAITING_FOR_RECOGNITION.name().equals(s.getStatus()))
                    .forEach(s -> sqsLoader.sendMessageToQueue(buildMsg(gv, s)));

            gv.setDuration(totalUs / US);                      // setter existant
            gv.setSegments(list);                              // setter existant

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return gv;
    }

    /* ------------- helper SQS ---------------- */
    private SqsVideoSegmentMessage buildMsg(GlobalVideoData gv, FileSegment s) {

        SqsVideoSegmentMessage.InputData in = new SqsVideoSegmentMessage.InputData();
        in.setVideoUrl(gv.getVideoSourceUrl());
        in.setSegmentId(s.getId());
        in.setS3Path(s.getS3Path());

        SqsVideoSegmentMessage msg = new SqsVideoSegmentMessage();
        msg.setAction("SpeechRead");
        msg.setGlobalJobId(gv.getJobId());
        msg.setStatus(ProcessingStatus.WAITING_FOR_RECOGNITION.name());
        msg.setOriginalLanguage(gv.getOriginalLanguage());
        msg.setTargetLanguage(gv.getTargetLanguage());
        msg.setInput(in);
        return msg;
    }
}
