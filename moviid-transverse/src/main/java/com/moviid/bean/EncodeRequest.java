package com.moviid.bean;

import com.moviid.bean.sqs.SqsVideoSegmentMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EncodeRequest {

    private String parentFolder;
    private String segmentId;
    private String job_id;
    private String bucket;
    private MoviidGrappe.SpeechData speechData;

    // Optionally, include SqsVideoSegmentMessage as a part of MergeRequest
    private SqsVideoSegmentMessage sqsMessage;

}
