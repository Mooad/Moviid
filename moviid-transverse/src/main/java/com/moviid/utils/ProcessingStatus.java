package com.moviid.utils;

public enum ProcessingStatus {
    WAITING_FOR_RECOGNITION,

    WAITING_FOR_SEGMENTATION,
    WAITING_FOR_TRANSLATION,

    WAITING_FOR_ENCODING,

    COMPLETED,
    FAILED,
    CANCELLED,
    MERGED
}