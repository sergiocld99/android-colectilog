package cs10.apps.travels.tracer.data.generator;

public class DelayData {
    private final int[][] DEMORA_MINUTOS = new int[Station.values().length][Station.values().length];

    public DelayData(){

        // -------------- RAMAL BOSQUES VIA TEMPERLEY ----------------------------
        add(Station.PLAZA, Station.YRIGOYEN, 37-32);
        add(Station.YRIGOYEN, Station.AVELLANEDA, 40-37);
        add(Station.AVELLANEDA, Station.GERLI, 43-40);
        add(Station.GERLI, Station.LANUS, 47-43);
        add(Station.LANUS, Station.ESCALADA, 50-47);
        add(Station.ESCALADA, Station.BANFIELD, 53-50);
        add(Station.BANFIELD, Station.LOMAS, 57-53);
        add(Station.LOMAS, Station.TEMPERLEY, 61-57);
        add(Station.TEMPERLEY, Station.MARMOL, 4-1);
        add(Station.MARMOL, Station.CALZADA, 7-4);
        add(Station.CALZADA, Station.CLAYPOLE, 10-7);
        add(Station.CLAYPOLE, Station.ARDIGO, 14-10);
        add(Station.ARDIGO, Station.VARELA, 19-14);
        add(Station.VARELA, Station.ZEBALLOS, 22-19);
        add(Station.ZEBALLOS, Station.BOSQUES, 26-22);

        // ---------------- RAMAL BOSQUES VIA QUILMES -------------------------------
        add(Station.BOSQUES, Station.SOURIGUES, 20-13);
        add(Station.SOURIGUES, Station.RANELAGH, 24-20);
        add(Station.RANELAGH, Station.VILLA_ESP, 28-24);
        add(Station.VILLA_ESP, Station.BERA, 32-28);
        add(Station.BERA, Station.EZPELETA, 35-32);
        add(Station.EZPELETA, Station.QUILMES, 41-35);
        add(Station.QUILMES, Station.BERNAL, 44-41);
        add(Station.BERNAL, Station.DON_BOSCO, 47-44);
        add(Station.DON_BOSCO, Station.WILDE, 49-47);
        add(Station.WILDE, Station.DOMINICO, 52-49);
        add(Station.DOMINICO, Station.SARANDI, 55-52);
        add(Station.SARANDI, Station.AVELLANEDA, 60-55);
        add(Station.AVELLANEDA, Station.PLAZA, 6);

        // ------------------ SERVICIOS DIRECTOS y SALTEOS -------------------------------
        add(Station.PLAZA, Station.LANUS, 37-26);       // Servicio Directo
        add(Station.LANUS, Station.LOMAS, 45-37);       // Servicio Directo
        add(Station.LOMAS, Station.MARMOL, 51-45);      // No para en Temperley
        add(Station.AVELLANEDA, Station.LANUS, 14-9);   // No para en Gerli

        // ------------------- RAMAL LA PLATA -------------------------------------
        add(Station.BERA, Station.PLATANOS, 4);
        add(Station.PLATANOS, Station.HUDSON, 3);
        add(Station.HUDSON, Station.PEREYRA, 7);
        add(Station.PEREYRA, Station.VILLA_ELISA, 3);
        add(Station.VILLA_ELISA, Station.CITY_BELL, 4);
        add(Station.CITY_BELL, Station.GONNET, 4);
        add(Station.GONNET, Station.RINGUELET, 3);
        add(Station.RINGUELET, Station.TOLOSA, 4);
        add(Station.TOLOSA, Station.LA_PLATA, 3);

        // -------------------- TREN UNIVERSITARIO ------------------------------
        add(Station.LA_PLATA, Station.ARQUI, 3);
        add(Station.ARQUI, Station.INFO, 3);
        add(Station.INFO, Station.MEDICINA, 4);
        add(Station.MEDICINA, Station.PERIODISMO, 3);
        add(Station.PERIODISMO, Station.DIAG_73, 4);
        add(Station.DIAG_73, Station.POLICLINICO, 3);

        // ------------------- RAMAL GLEW / KORN --------------------------------
        add(Station.KORN, Station.GUERNICA, 6);
        add(Station.GUERNICA, Station.GLEW, 4);
        add(Station.GLEW, Station.LONGCHAMPS, 4);
        add(Station.LONGCHAMPS, Station.BURZACO, 6);
        add(Station.BURZACO, Station.ADROGUE, 3);
        add(Station.ADROGUE, Station.TEMPERLEY, 4);
        add(Station.ADROGUE, Station.PLAZA, 26);
    }

    // add both sides
    private void add(Station s1, Station s2, int offsetTime){
        addOnly(s1, s2, offsetTime);
        addOnly(s2, s1, offsetTime);
    }

    private void addOnly(Station s1, Station s2, int offsetTime){
        DEMORA_MINUTOS[s1.ordinal()][s2.ordinal()] = offsetTime;
    }

    public int getDelay(int s1, int s2){
        int value = DEMORA_MINUTOS[s1][s2];
        if (value == 0) throw new RuntimeException("Delay for [" + s1 + "," + s2 + "] is undefined");
        return value;
    }

    public int getDelay(Station s1, Station s2){
        return getDelay(s1.ordinal(), s2.ordinal());
    }
}
