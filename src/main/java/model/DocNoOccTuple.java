package model;

public class DocNoOccTuple {
    private String documentName;
    private Integer noOcc;

    public DocNoOccTuple(String documentName, Integer noOcc) {
        this.documentName = documentName;
        this.noOcc = noOcc;
    }

    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    public Integer getNoOcc() {
        return noOcc;
    }

    public void setNoOcc(Integer noOcc) {
        this.noOcc = noOcc;
    }
}
