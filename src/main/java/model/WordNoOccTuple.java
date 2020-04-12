package model;

public class WordNoOccTuple {
    private String word;
    private Integer noOcc;

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public Integer getNoOcc() {
        return noOcc;
    }

    public void setNoOcc(Integer noOcc) {
        this.noOcc = noOcc;
    }

    public WordNoOccTuple(String word, Integer noOcc) {
        this.word = word;
        this.noOcc = noOcc;
    }
}
