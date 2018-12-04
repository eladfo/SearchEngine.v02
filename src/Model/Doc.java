package Model;

public class Doc {
    public StringBuilder docID = new StringBuilder();
    public StringBuilder docDate = new StringBuilder();
    public String docFile;
    public StringBuilder docText = new StringBuilder();
    public StringBuilder docCity = new StringBuilder();


    public void update(String st, int flag)
    {
        switch (flag) {
            case 0:
                    break;
            case 1: docID.append(st);
                    break;
            case 2: docCity.append(st); //Might worth change to append().append()
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

    public void resetDoc()
    {
        docID.setLength(0);
        docDate.setLength(0);
        docText.setLength(0);
        docCity.setLength(0);
    }

    public StringBuilder getDocCity()
    {
        return docCity;
    }
}
