import DBSM.Relation;

public class Main {
    public static void main(String[] args){

        /*
        String[] fn = {"Cognome", "Nome", "Data", "MateriaP"};
        Relation r = new Relation("Studente", fn);

        String[] s = {"Rossi", "Mario", "1-12-2000", "Italiano"};
        r.insert(s);
        String[] s1 = {"Bellardi", "Michele", "7-8-2007", "Inglese"};
        r.insert(s1);
        String[] s2 = {"Rigo", "Jacopo", "25-5-2007", "Informatica"};
        r.insert(s2);

        r.save("./");
        */

        Relation r = Relation.load("./", "Studente");

        Relation r1 = Relation.load("./", "Aule");

        Relation r2 = Relation.load("./", "Docenti");

        if(r != null && r1 != null && r2 != null){
            System.out.println("PROVA SELECTION");
            System.out.println(r.selection("MateriaP = 'Italiano'"));

            System.out.println("PROVA PROJECTION");
            System.out.println(r.projection(new String[]{"Nome", "Cognome"}));

            System.out.println("PROVA CROSSPRODUCT");
            //System.out.println(r1.rename("Cicci", new String[]{"Paolino", "Paperino"}));
            System.out.println(r.crossProduct(r1));

            /* // ERRORE! UNION NON FUNZIONA, perche r e r2 non hanno gli stessi campi,
             *    la join va fatta solo su relazioni con campi uguali
             * System.out.println("PROVA UNION");
             * Relation ru = r.union(r2);
             * System.out.println(ru);
             *
             * System.out.println("PROVA DIFFERENCE");
             * System.out.println(ru.difference(r));
             *
             */



            System.out.println("PROVA AGGIORNAMENTO SELECTION");
            Relation c = r.crossProduct(r1);
            System.out.println(c.selection("Cognome = ClasseId"));

            System.out.println("PROVA JOIN");
            System.out.println(r.join(r1, "Classe = ClasseId"));


        }



        //
    }
}