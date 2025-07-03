package com.moviid.vidprocessor.mappers;


import com.moviid.bean.FileSegment;
import com.moviid.bean.MoviidGrappe;

public class SegmentMapper {

    public static MoviidGrappe.Segment mapFileSegmentToSegment(FileSegment fileSegment) {
        if (fileSegment == null) {
            return null; // No segment to map
        }

        MoviidGrappe.Segment segment = new MoviidGrappe.Segment();
        segment.setSegmentID(fileSegment.getId());
        segment.setStartTime((int) (fileSegment.getStartTimestamp() / 1000)); // Assuming startTimestamp is in milliseconds
        segment.setEndTime((int) (fileSegment.getEndTimestamp() / 1000)); // Assuming endTimestamp is in milliseconds
        segment.setDuration((int) ((fileSegment.getEndTimestamp() - fileSegment.getStartTimestamp()) / 1000)); // Duration in seconds
        segment.setSegmentUrl(fileSegment.getFilePath());

        // Map the S3 path
        segment.setS3Path(fileSegment.getS3Path());

        // Map the status of the segment
        segment.setStatus(fileSegment.getStatus());

        // Map the parent job ID
        segment.setParentJobId(fileSegment.getParentJobId());

        // If you have OCRData, translations, subtitles, you would map them here as well
        // Example:
        // segment.setOcrData(mapOCRData(fileSegment.getOCRData()));
        // segment.setTranslations(mapTranslations(fileSegment.getTranslations()));
        // segment.setSubtitles(mapSubtitles(fileSegment.getSubtitles()));

        return segment;
    }

    // If there are other fields like OCRData, translations, subtitles, you can create helper methods to map those as well
    // Example:
    // private static MoviidGrappe.Segment.OCRData mapOCRData(OCRData ocrData) {
    //     // Map OCRData fields
    //     return new MoviidGrappe.Segment.OCRData(...);
    // }

    // Similarly for translations and subtitles
}
