package DBSM;

import java.io.*;
import java.util.ArrayList;

public class Relation {

    private String name;
    private String[] fields_name;
    private ArrayList<String[]> data;

    public Relation(String n, String[] fn) {

        name = n;
        fields_name = new String[fn.length];
        System.arraycopy(fn, 0, fields_name, 0, fn.length);

        data = new ArrayList<String[]>();

    }
    // inserisce una nuova riga nella re
    public void insert(String[] row){
        if(!isDuplicated(row)){
            String[] r = new String[row.length];
            System.arraycopy(row, 0, r, 0, r.length);
            data.add(r);
        }
    }

    // ritorna true se una riga con gli stessi valori è presente
    public boolean isDuplicated(String[] new_row){
        boolean equals = true;

        for(String[] row : data){
            equals = true;
            for(int i = 0; i < row.length; i++){
                if(!row[i].equals(new_row[i])) {
                    equals = false;
                    i = row.length;
                }
            }
            if(equals) return true;
        }

        return false;
    }

    public void save(String path){
        try{
            PrintWriter writer = new PrintWriter(new FileWriter(path + name + ".csv"));

            for(String fn : fields_name){
                writer.print(fn + ",");
            }
            writer.println("");

            for(String[] row : data){
                for(String f : row){
                    writer.print(f + ",");
                }
                writer.println("");
            }

            writer.flush();
            writer.close();
        }catch(IOException ioe){
            System.out.println(ioe.getMessage());
        }
    }

    public static Relation load(String path, String name){
        try{
            BufferedReader reader = new BufferedReader(new FileReader(path + name + ".csv"));
            // legge la riga di intestazione
            String line = reader.readLine();
            String[] fn = line.split(",");
            Relation res = new Relation(name, fn);

            // Legge tutte le righe del database
            String[] row;
            while ((line = reader.readLine()) != null){
                row = line.split(",");
                res.insert(row);
            }

            reader.close();

            return res;
        }catch (IOException ioe){
            System.out.println(ioe.getMessage());
        }

        return null;
    }

    @Override
    public String toString() {
        int length_row = 20 * fields_name.length + (fields_name.length - 1);
        String res = printLine() + "\n";
        res += "|" + String.format("%-" + length_row + "s", name) + "|\n";
        res += printLine() + "\n";

        res += "|";
        for(int i = 0; i < fields_name.length; i++) {
            res += String.format("%-20s", fields_name[i]) + "|";
        }

        res += "\n" + printLine() + "\n";

        for(String[] row : data){
            res += "|";
            for(int i = 0; i < fields_name.length; i++){
                res += String.format("%-20s", row[i]) + "|";
            }
            res += "\n" + printLine() + "\n";
        }

        return res;
    }

    private int getFieldIndex(String fn){
        for(int i = 0; i < fields_name.length; i++){
            if (fn.equals(fields_name[i])) return i;
        }

        return -1;
    }

    private String getValue(String raw_data, String[] row){
        // se la stringa passata in parametro inizia con ' ritorno il valore compreso fra i due apici
        // altrimenti il valore della colonna corrispondente riga passata in parametro

        if(raw_data.startsWith("'")){
            return raw_data.substring(1, raw_data.length()-1);
        } else {
            return row[getFieldIndex(raw_data)];
        }
    }


    public Relation selection(String condition){
        // devo definire le condizioni stringa "campo = valore" o "campo = campo"

        Relation res = new Relation("R1", fields_name);

        //0 è campo, 1 è operatore, 2 è valore
        String[] cond = condition.split(" ");
        int fi = getFieldIndex(cond[0]);
        String value; // il valore su cui faccio il confronto del campo

        if (fi != -1){
            for(String[] row : data){
                value = getValue(cond[2], row);

                if (cond[1].equals("=")){
                    if (value.equals(row[fi])){
                        res.insert(row);
                    }
                } else if (cond[1].equals("<>")) {
                    if (!value.equals(row[fi])){
                        res.insert(row);
                    }
                }

            }
        }

        return res;
    }

    public Relation projection (String[] fn){
        Relation res = new Relation("R1", fn);

        // Recupero gli indici di tutte le colonne
        // suppongo che le colonne esistano
        int[] field_index = new int[fn.length];
        for(int i = 0; i < field_index.length; i++){
            field_index[i] = getFieldIndex(fn[i]);
        }

        //crea array di appoggio per nuova riga
        String[] new_row = new String[fn.length];
        // copio per ogni riga in data solo le colonne che mi interessano
        // e inserisco la nuova riga nella nuova relazione
        for(String[] row : data){
            for(int i = 0; i < new_row.length; i++){
                new_row[i] = row[field_index[i]];
            }
            res.insert(new_row);
        }

        return res;
    }

    // ritorna una nuova relazione con il nome della relazione e dei campi
    public Relation rename(String name, String[] fn){
        Relation res = new Relation(name, fn);

        for(String[] row : data){
            res.insert(row);
        }

        return res;
    }

    // ritorna una nuova relazione con i campi della prima e della seconda
    // e in ogni riga i valori di ogni riga della relazione associati alle
    // righe di r
    public Relation crossProduct(Relation r){
        // Creo l'array dei nuovi fields name
        String[] fn = new String[fields_name.length + r.fields_name.length];
        System.arraycopy(fields_name, 0, fn, 0, fields_name.length);
        System.arraycopy(r.fields_name, 0, fn, fields_name.length, r.fields_name.length);

        // Creo la nuova relazione
        Relation res = new Relation("R1", fn);

        // Creo un supporto per la nuova riga
        String[] new_row = new String[fn.length];

        for(String[] ra : data){
            System.arraycopy(ra, 0, new_row, 0, ra.length);
            for(String[] rb : r.data){
                System.arraycopy(rb, 0, new_row, ra.length, rb.length);
                res.insert(new_row);
            }
        }

        return res;
    }

    // Ritorna una nuova relazione con i campi della relazione di origine
    // e le righe della prima e della seconda senza duplicati
    public Relation union (Relation r){
        // Creo la nuova relazione
        Relation res = new Relation("R", fields_name);

        for(String[] row : data){
            res.insert(row);
        }
        for(String[] row : r.data){
            res.insert(row);
        }

        return res;
    }

    // Ritorna una nuova relazione con i campi della relazione di origine
    // e le righe della prima meno gli elementi della seconda
    public Relation difference(Relation r){
        // Creo la nuova relazione
        Relation res = new Relation("R", fields_name);

        for(String[] row : data){
            if (!r.isDuplicated(row)){
                res.insert(row);
            }
        }

        return res;
    }

    public Relation join(Relation r, String cond){
        // Associa ad ogni riga di this le righe di r per cui è soddisfatta la condizionenella forma
        // campo1 = campo2 con campo1 in this e campo2 in r

        String[] fn = new String[fields_name.length + r.fields_name.length];
        System.arraycopy(fields_name, 0, fn, 0, fields_name.length);
        System.arraycopy(r.fields_name, 0, fn, fields_name.length, r.fields_name.length);

        Relation res = new Relation("R", fn);

        String[] condition = cond.split(" ");

        int fi1 = getFieldIndex(condition[0]);
        int fi2 = r.getFieldIndex(condition[2]);

        String[] new_row = new String[fn.length];
        for(String[] rt : data){
            System.arraycopy(rt, 0, new_row, 0 , rt.length);
            for(String[] rr : r.data){
                if(rt[fi1].equals(rr[fi2])){
                    System.arraycopy(rr, 0, new_row, rt.length, rr.length);
                    res.insert(new_row);
                }
            }
        }

        return res;
    }

    private String printLine(){
        int length_row = 20 * fields_name.length + (fields_name.length-1);
        String res = "+";
        res += String.format("%-" + length_row + "s", "").replace(" ", "-");
        res += "+";
        return res;
    }
}





