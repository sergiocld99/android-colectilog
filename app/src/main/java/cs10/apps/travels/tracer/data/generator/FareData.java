package cs10.apps.travels.tracer.data.generator;

public class FareData {
    private final int[][] SECCION_DATA = new int[Station.values().length][Station.values().length];
    private final double[] SECCION_TARIFA = new double[]{130, 169, 208, 19.00};

    // se asignan las tarifas que son distintas de la sección 0
    public FareData(){
        // Universitario
        setRange(Station.LA_PLATA, Station.ARQUI, Station.POLICLINICO, 3);
        setRange(Station.ARQUI, Station.INFO, Station.POLICLINICO, 3);
        setRange(Station.INFO, Station.MEDICINA, Station.POLICLINICO, 3);
        setRange(Station.MEDICINA, Station.PERIODISMO, Station.POLICLINICO, 3);
        setRange(Station.PERIODISMO, Station.DIAG_73, Station.POLICLINICO, 3);
        add(Station.DIAG_73, Station.POLICLINICO, 3);

        // C1
        setRange(Station.PLAZA, Station.BANFIELD, Station.LONGCHAMPS, 1);
        setRange(Station.PLAZA, Station.GLEW, Station.KORN, 2);
        setRange(Station.PLAZA, Station.MARMOL, Station.ARDIGO, 1);
        setRange(Station.PLAZA, Station.VARELA, Station.BOSQUES, 2);
        setRange(Station.PLAZA, Station.EZPELETA, Station.HUDSON, 1);
        setRange(Station.PLAZA, Station.PEREYRA, Station.LA_PLATA, 2);

        // C2
        setRange(Station.YRIGOYEN, Station.TEMPERLEY, Station.GLEW, 1);
        setRange(Station.YRIGOYEN, Station.GUERNICA, Station.KORN, 2);
        setRange(Station.YRIGOYEN, Station.MARMOL, Station.ZEBALLOS, 1);
        add(Station.YRIGOYEN, Station.BOSQUES, 2);
        setRange(Station.YRIGOYEN, Station.BERA, Station.PEREYRA, 1);
        setRange(Station.YRIGOYEN, Station.VILLA_ELISA, Station.LA_PLATA, 2);

        // C3
        setRange(Station.AVELLANEDA, Station.TEMPERLEY, Station.GLEW, 1);
        setRange(Station.AVELLANEDA, Station.GUERNICA, Station.KORN, 2);
        setRange(Station.AVELLANEDA, Station.MARMOL, Station.BOSQUES, 1);
        setRange(Station.AVELLANEDA, Station.VILLA_ESP, Station.VILLA_ELISA, 1);
        setRange(Station.AVELLANEDA, Station.CITY_BELL, Station.LA_PLATA, 2);

        // C4
        setRange(Station.GERLI, Station.ADROGUE, Station.GUERNICA, 1);
        add(Station.GERLI, Station.KORN, 2);
        setRange(Station.GERLI, Station.MARMOL, Station.BOSQUES, 1);
        setRange(Station.GERLI, Station.BERNAL, Station.RANELAGH, 1);
        add(Station.GERLI, Station.SOURIGUES, 2);
        setRange(Station.GERLI, Station.PLATANOS, Station.HUDSON, 1);
        setRange(Station.GERLI, Station.PEREYRA, Station.LA_PLATA, 2);

        // C5
        setRange(Station.LANUS, Station.BURZACO, Station.GUERNICA, 1);
        add(Station.LANUS, Station.KORN, 2);
        setRange(Station.LANUS, Station.CLAYPOLE, Station.BOSQUES, 1);
        setRange(Station.LANUS, Station.WILDE, Station.VILLA_ESP, 1);
        add(Station.LANUS, Station.RANELAGH, 2);
        add(Station.LANUS, Station.SOURIGUES, 1);
        setRange(Station.LANUS, Station.PLATANOS, Station.LA_PLATA, 2);

        // C6
        setRange(Station.ESCALADA, Station.LONGCHAMPS, Station.KORN, 1);
        setRange(Station.ESCALADA, Station.ARDIGO, Station.BOSQUES, 1);
        setRange(Station.ESCALADA, Station.DOMINICO, Station.BERA, 1);
        add(Station.ESCALADA, Station.VILLA_ESP, 2);
        setRange(Station.ESCALADA, Station.RANELAGH, Station.SOURIGUES, 1);
        setRange(Station.ESCALADA, Station.PLATANOS, Station.LA_PLATA, 2);

        // C7
        setRange(Station.BANFIELD, Station.LONGCHAMPS, Station.KORN, 1);
        setRange(Station.BANFIELD, Station.ARDIGO, Station.BOSQUES, 1);
        setRange(Station.BANFIELD, Station.SARANDI, Station.EZPELETA, 1);
        add(Station.BANFIELD, Station.BERA, 2);
        setRange(Station.BANFIELD, Station.VILLA_ESP, Station.SOURIGUES, 1);
        setRange(Station.BANFIELD, Station.PLATANOS, Station.LA_PLATA, 2);

        // C8
        setRange(Station.LOMAS, Station.GLEW, Station.KORN, 1);
        setRange(Station.LOMAS, Station.VARELA, Station.BOSQUES, 1);
        setRange(Station.LOMAS, Station.SARANDI, Station.QUILMES, 1);
        add(Station.LOMAS, Station.EZPELETA, 2);
        setRange(Station.LOMAS, Station.BERA, Station.SOURIGUES, 1);
        setRange(Station.LOMAS, Station.PLATANOS, Station.LA_PLATA, 2);

        // C9
        setRange(Station.TEMPERLEY, Station.GUERNICA, Station.KORN, 1);
        setRange(Station.TEMPERLEY, Station.SARANDI, Station.RANELAGH, 1);
        setRange(Station.TEMPERLEY, Station.PLATANOS, Station.CITY_BELL, 1);
        setRange(Station.TEMPERLEY, Station.GONNET, Station.LA_PLATA, 2);

        // C10
        setRange(Station.ADROGUE, Station.GUERNICA, Station.KORN, 1);
        setRange(Station.ADROGUE, Station.VARELA, Station.BOSQUES, 1);
        setRange(Station.ADROGUE, Station.SARANDI, Station.BERNAL, 1);
        setRange(Station.ADROGUE, Station.QUILMES, Station.EZPELETA, 2);
        setRange(Station.ADROGUE, Station.BERA, Station.SOURIGUES, 1);
        setRange(Station.ADROGUE, Station.PLATANOS, Station.LA_PLATA, 2);

        // C31
        setRange(Station.MARMOL, Station.SARANDI, Station.BERNAL, 1);
        add(Station.MARMOL, Station.QUILMES, 2);
        setRange(Station.MARMOL, Station.EZPELETA, Station.VILLA_ESP, 1);
        setRange(Station.MARMOL, Station.PLATANOS, Station.GONNET, 1);
        setRange(Station.MARMOL, Station.RINGUELET, Station.LA_PLATA, 2);

        // C32
        setRange(Station.CALZADA, Station.SARANDI, Station.EZPELETA, 1);
        setRange(Station.CALZADA, Station.PLATANOS, Station.RINGUELET, 1);
        setRange(Station.CALZADA, Station.TOLOSA, Station.LA_PLATA, 2);

        // C33
        setRange(Station.CLAYPOLE, Station.SARANDI, Station.WILDE, 1);
        add(Station.CLAYPOLE, Station.DON_BOSCO, 2);
        setRange(Station.CLAYPOLE, Station.BERNAL, Station.EZPELETA, 1);
        setRange(Station.CLAYPOLE, Station.PLATANOS, Station.TOLOSA, 1);
        add(Station.CLAYPOLE, Station.LA_PLATA, 2);

        // C34
        setRange(Station.ARDIGO, Station.SARANDI, Station.QUILMES, 1);
        setRange(Station.ARDIGO, Station.HUDSON, Station.LA_PLATA, 1);

        // C35
        setRange(Station.VARELA, Station.SARANDI, Station.BERNAL, 1);
        add(Station.VARELA, Station.PEREYRA, 1);
        setRange(Station.VARELA, Station.CITY_BELL, Station.LA_PLATA, 1);

        // C36
        setRange(Station.ZEBALLOS, Station.SARANDI, Station.DON_BOSCO, 1);
        setRange(Station.ZEBALLOS, Station.CITY_BELL, Station.LA_PLATA, 1);

        // C37
        setRange(Station.BOSQUES, Station.SARANDI, Station.WILDE, 1);
        setRange(Station.BOSQUES, Station.GONNET, Station.LA_PLATA, 1);

        // C38
        add(Station.SARANDI, Station.SOURIGUES, 1);
        setRange(Station.SARANDI, Station.HUDSON, Station.CITY_BELL, 1);
        setRange(Station.SARANDI, Station.GONNET, Station.LA_PLATA, 2);

        // C39
        setRange(Station.DOMINICO, Station.PEREYRA, Station.GONNET, 1);
        setRange(Station.DOMINICO, Station.RINGUELET, Station.LA_PLATA, 2);

        // C40
        setRange(Station.WILDE, Station.PEREYRA, Station.RINGUELET, 1);
        setRange(Station.WILDE, Station.TOLOSA, Station.LA_PLATA, 2);

        // C41
        setRange(Station.DON_BOSCO, Station.PEREYRA, Station.RINGUELET, 1);
        setRange(Station.DON_BOSCO, Station.TOLOSA, Station.LA_PLATA, 2);

        // C42
        setRange(Station.BERNAL, Station.PEREYRA, Station.TOLOSA, 1);
        add(Station.BERNAL, Station.LA_PLATA, 2);

        // C43 en adelante
        setRange(Station.QUILMES, Station.VILLA_ELISA, Station.LA_PLATA, 1);
        setRange(Station.EZPELETA, Station.CITY_BELL, Station.LA_PLATA, 1);
        setRange(Station.BERA, Station.GONNET, Station.LA_PLATA, 1);
        setRange(Station.VILLA_ESP, Station.CITY_BELL, Station.LA_PLATA, 1);
        setRange(Station.RANELAGH, Station.CITY_BELL, Station.LA_PLATA, 1);
        setRange(Station.SOURIGUES, Station.CITY_BELL, Station.LA_PLATA, 1);
        setRange(Station.PLATANOS, Station.TOLOSA, Station.LA_PLATA, 1);
        setRange(Station.HUDSON, Station.TOLOSA, Station.LA_PLATA, 1);

        // C51: de Pereyra a Tolosa hasta La Plata son todos Sección 0 -> no hacer nada
    }

    private void add(Station s1, Station s2, int section){
        SECCION_DATA[s1.ordinal()][s2.ordinal()] = section;
        SECCION_DATA[s2.ordinal()][s1.ordinal()] = section;
    }

    private void setRange(Station origin, Station since, Station until, int section){
        for (int i=since.ordinal(); i<=until.ordinal(); i++){
            add(origin, Station.values()[i], section);
        }
    }

    public double getTarifa(Station s1, Station s2){
        if (s1 == s2) return 0;
        return SECCION_TARIFA[SECCION_DATA[s1.ordinal()][s2.ordinal()]];
    }

    public double getTarifa(Station s1, String s2) {
        Station fin = Station.findByNombre(s2);
        if (fin == null) return 0;
        return getTarifa(s1, fin);
    }
}
