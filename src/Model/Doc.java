package Model;

public class Doc {
    private StringBuilder docID = new StringBuilder();
    //public StringBuilder docDate = new StringBuilder();
    private String docFile;
    private StringBuilder docText = new StringBuilder();
    private StringBuilder docCity = new StringBuilder();

    /**
     * Insert the line to the specific field depends on the flag.
     * @param st - line of text
     * @param flag - switch case
     */
    public void update(String st, int flag)
    {
        switch (flag) {
            case 0:
                    break;
            case 1: docID.append(st);
                    break;
            case 2: docCity.append(st);
                    break;
            case 4: docText.append(st).append(" ");
                    break;
        }
    }

    public StringBuilder getDocID() {
        return docID;
    }

    public StringBuilder getDocText() {
        return docText;
    }

    public StringBuilder getDocCity()
    {
        return docCity;
    }

    public String getDocFile() {
        return docFile;
    }

    public void setDocFile(String docFile) {
        this.docFile = docFile;
    }
}
