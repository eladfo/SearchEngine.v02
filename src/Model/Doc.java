package Model;

public class Doc {
    private StringBuilder docID = new StringBuilder();
    private StringBuilder docDate = new StringBuilder();
    private StringBuilder docHeader = new StringBuilder();
    public StringBuilder docText = new StringBuilder();
    private StringBuilder docCity = new StringBuilder();


    public void update(String st, int flag)
    {
        switch (flag) {
            case 0:
                    break;
            case 1: docID.append(st);
                    break;
            case 2: docCity.append(st); //Might worth change to append().append()
                    break;
            case 3: docHeader.append(st); //Might worth change to append().append()
                    break;
            case 4: docText.append(st);
                    break;
        }
    }

    public StringBuilder getDocID() {
        return docID;
    }

    public StringBuilder getDocDate() {
        return docDate;
    }

    public StringBuilder getDocHeader() {
        return docHeader;
    }

    public StringBuilder getDocText() {
        return docText;
    }

    public void setDocText(StringBuilder docText) {
        this.docText = docText;
    }

    public void resetDoc()
    {
        docID.setLength(0);
        docDate.setLength(0);
        docHeader.setLength(0);
        docText.setLength(0);
        docCity.setLength(0);
    }

    public StringBuilder getDocCity()
    {
        return docCity;
    }
}
