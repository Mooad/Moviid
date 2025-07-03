package com.moviid.vidprocessor.service.metadata;


import com.moviid.bean.FileSegment;
import com.moviid.bean.GlobalVideoData;
import com.moviid.bean.MoviidGrappe;
import com.moviid.utils.ProcessingStatus;
import com.moviid.vidprocessor.mappers.SegmentMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class SectionsMetadataServiceImpl implements SectionsMetadataService {

    @Override
    public MoviidGrappe updateVideoSectionData(GlobalVideoData globalVideoData) {
        MoviidGrappe moviidGrappe = new MoviidGrappe();

        moviidGrappe.setTitle(globalVideoData.getTitle());
        moviidGrappe.setDuration(globalVideoData.getDuration());
        moviidGrappe.setProcessingStatus(ProcessingStatus.WAITING_FOR_RECOGNITION.name());
        moviidGrappe.setOriginalLanguage(globalVideoData.getOriginalLanguage());
        moviidGrappe.setTargetLanguage(globalVideoData.getTargetLanguage());
        moviidGrappe.setVideoSourceUrl(globalVideoData.getVideoSourceUrl());
        moviidGrappe.setJobId(globalVideoData.getJobId());
        moviidGrappe.setSegments(new ArrayList<>());

        for (FileSegment fileSegment : globalVideoData.getSegments())
        {
            moviidGrappe.getSegments().add(SegmentMapper.mapFileSegmentToSegment(fileSegment));
        }

        return moviidGrappe;
    }




}


