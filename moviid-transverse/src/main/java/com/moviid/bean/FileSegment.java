package com.moviid.bean;

public class FileSegment {
    private String id;
    private String filePath;
    private int segmentNumber;
    private long startTimestamp;
    private long endTimestamp;
    private String uniqueParentFolderName;
    private Integer numOrder;
    private String status; // To track the processing status of the segment
    private String parentJobId; // To link back to the parent job ID

    private String videoSourceUrl;
    private String s3Path; // S3 path of the segment

    // Constructor
    public FileSegment(String id, String filePath, int segmentNumber, long startTimestamp, long endTimestamp, String uniqueParentFolderName, Integer numOrder, String status, String parentJobId, String videoSourceUrl, String s3Path) {
        this.id = id;
        this.filePath = filePath;
        this.segmentNumber = segmentNumber;
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
        this.uniqueParentFolderName = uniqueParentFolderName;
        this.numOrder = numOrder;
        this.status = status;
        this.parentJobId = parentJobId;
        this.videoSourceUrl = videoSourceUrl;
        this.s3Path = s3Path;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public int getSegmentNumber() { return segmentNumber; }
    public void setSegmentNumber(int segmentNumber) { this.segmentNumber = segmentNumber; }

    public long getStartTimestamp() { return startTimestamp; }
    public void setStartTimestamp(long startTimestamp) { this.startTimestamp = startTimestamp; }

    public long getEndTimestamp() { return endTimestamp; }
    public void setEndTimestamp(long endTimestamp) { this.endTimestamp = endTimestamp; }

    public String getUniqueParentFolderName() { return uniqueParentFolderName; }
    public void setUniqueParentFolderName(String uniqueParentFolderName) { this.uniqueParentFolderName = uniqueParentFolderName; }

    public Integer getNumOrder() { return numOrder; }
    public void setNumOrder(Integer numOrder) { this.numOrder = numOrder; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getParentJobId() { return parentJobId; }
    public void setParentJobId(String parentJobId) { this.parentJobId = parentJobId; }

    public String getVideoSourceUrl() { return videoSourceUrl; }
    public void setVideoSourceUrl(String videoSourceUrl) { this.videoSourceUrl = videoSourceUrl; }

    public String getS3Path() { return s3Path; }
    public void setS3Path(String s3Path) { this.s3Path = s3Path; }

    @Override
    public String toString() {
        return "FileSegment{" +
                "id='" + id + '\'' +
                ", filePath='" + filePath + '\'' +
                ", segmentNumber=" + segmentNumber +
                ", startTimestamp=" + startTimestamp +
                ", endTimestamp=" + endTimestamp +
                ", uniqueParentFolderName='" + uniqueParentFolderName + '\'' +
                ", numOrder=" + numOrder +
                ", status='" + status + '\'' +
                ", parentJobId='" + parentJobId + '\'' +
                ", videoSourceUrl='" + videoSourceUrl + '\'' +
                ", s3Path='" + s3Path + '\'' +
                '}';
    }
}
