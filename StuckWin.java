/**
 * Université de Franche-Comté, IUT Nord Franche-Comté, 90000 Belfort
 * Année Universitaire 2022-2023
 * SAE S1 01 / Groupe 29
 * Nathan BOSCHI [nathan.boschi@edu.univ-fcomte.fr]
 * Jessy MOUGAMMADALY [jessy.mougammadaly@edu.univ-fcomte.fr]
 */

import java.io.File;
import java.io.FileWriter;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.time.LocalDateTime;

public class StuckWin {
    static final Scanner input = new Scanner(System.in);
    private static final double BOARD_SIZE = 7;
    static final double PIECE_RADIUS = 0.35;
    static final double HEXAGON_RADIUS = 0.5;
    static final int DEFAULT_SPACE_NUMBER = 5;

    File curCsvFile;
    int displayMode = 2;

    enum Result {OK, BAD_COLOR, DEST_NOT_FREE, EMPTY_SRC, TOO_FAR, EXT_BOARD, EXIT}

    enum ModeMvt {REAL, SIMU}

    final char[] joueurs = {'B', 'R'};
    static final int SIZE = 8;
    static final char VIDE = '.';
    // 'B'=bleu 'R'=rouge '.'=vide '-'=n'existe pas
    char[][] state = {
            {'-', '-', '-', '-', 'R', 'R', 'R', 'R'},
            {'-', '-', '-', '.', 'R', 'R', 'R', 'R'},
            {'-', '-', '.', '.', '.', 'R', 'R', 'R'},
            {'-', 'B', 'B', '.', '.', '.', 'R', 'R'},
            {'-', 'B', 'B', 'B', '.', '.', '.', '-'},
            {'-', 'B', 'B', 'B', 'B', '.', '-', '-'},
            {'-', 'B', 'B', 'B', 'B', '-', '-', '-'},
    };

    /**
     * Déplace un pion ou simule son déplacement
     * @param couleur couleur du pion à déplacer
     * @param lcSource case source Lc
     * @param lcDest case destination Lc
     * @param mode ModeMVT.REAL/SIMU selon qu'on réalise effectivement le déplacement ou qu'on le simule seulement.
     * @return enum {OK, BAD_COLOR, DEST_NOT_FREE, EMPTY_SRC, TOO_FAR, EXT_BOARD, EXIT} selon le déplacement
     */
    Result deplace(char couleur, String lcSource, String lcDest,  ModeMvt mode) {
        if(lcDest.equals("q")) {
            return Result.EXIT;
        }
        Result result = Result.TOO_FAR;
        if(isOutOfBound(lcSource) || isOutOfBound(lcDest)) {
            result = Result.EXT_BOARD;
        }
        int rowSrc = charToInt(lcSource.charAt(0));
        int colSrc = Character.getNumericValue(lcSource.charAt(1));
        int rowDest = charToInt(lcDest.charAt(0));
        int colDest = Character.getNumericValue(lcDest.charAt(1));
        char charSrc = this.state[rowSrc][colSrc];
        char charDest = this.state[rowDest][colDest];
        String[] possibleDest = possibleDests(couleur, rowSrc, colSrc);
        if(charSrc == '-' || charDest == '-') {
            result = Result.EXT_BOARD;
        }else if(charDest != VIDE) {
            result = Result.DEST_NOT_FREE;
        }else if(charSrc == VIDE) {
            result = Result.EMPTY_SRC;
        }else if(charSrc != couleur) {
            result = Result.BAD_COLOR;
        }
        for(int i = 0; i < possibleDest.length; i++) {
            if(possibleDest[i].equals(lcDest)) {
                result = Result.OK;
                break;
            }
        }
        if(result == Result.OK) {
            this.state[rowDest][colDest] = charSrc;
            this.state[rowSrc][colSrc] = VIDE;
        }
        return result;
    }

    /**
     * Vérifie si la position passée en paramètre n'est pas dans les limites de la zone de jeu
     *
     * @param position case dont la position va être testée
     * @return vrai si la case n'est pas dans le plateau de jeu, faux si elle l'est.
     */
    boolean isOutOfBound(String position) {
        int i = charToInt(position.charAt(0));
        int j = Character.getNumericValue(position.charAt(1));
        boolean isIPositionOut = i < 0 || i >= BOARD_SIZE;
        boolean isJPositionOut = j < 0 || j > BOARD_SIZE;
        return isIPositionOut || isJPositionOut;
    }

    /**
     * Convertit un caractère en nombre (ex: A -> 0 et B -> 1)
     * @param character le caractère à convertir
     * @return le nombre correspondant au caractère entré
     */
    int charToInt(char character) {
        return (character - 'A');
    }

    /**
     * Convertit un nombre en caractère (ex: 0 -> A et 1 -> B)
     * @param number le nombre à convertir
     * @return le caractère correspondant au nombre entré
     */
    char intToChar(int number) {
        return (char)(number + 'A');
    }

    /**
     * Construit les trois chaînes représentant les positions accessibles
     * à partir de la position de départ [idLettre][idCol].
     * @param couleur couleur du pion à jouer
     * @param idLettre id de la ligne du pion à jouer
     * @param idCol id de la colonne du pion à jouer
     * @return tableau des trois positions jouables par le pion (redondance possible sur les bords)
     */
    String[] possibleDests(char couleur, int idLettre, int idCol){
        String[] result = new String[]{"", "", ""};
        if(this.state[idLettre][idCol] != couleur) {
            return result;
        }
        int orientation = (couleur == 'B' ? (-1) : 1);
        for(int i = 0; i < 3; i++) {
            int x = idCol + orientation * (int) Math.round(Math.cos(2 * Math.PI * i / 8.0 + (Math.PI / 2)));
            int y = idLettre + orientation * (int) Math.round(Math.sin(2 * Math.PI * i / 8.0 + (Math.PI / 2)));
            if (x >= 0 && y >= 0 && x < (BOARD_SIZE + 1) && y < BOARD_SIZE && this.state[y][x] == VIDE) {
                result[i] = "" + intToChar(y) + x;
            }
        }

        return result;
    }

    /**
     * Affiche le plateau de jeu dans la configuration portée par
     * l'attribut d'état "state" dans la console
     */
    void affiche() {
        for (int i = this.state[0].length - 1; i > (-(this.state[0].length)); i--) {
            StringBuilder line = new StringBuilder("");
            int nbSpace = DEFAULT_SPACE_NUMBER;
            for (int j = 0; j < this.state.length; j++) {
                if ((i + j) >= 0 && (i + j) < this.state[0].length && this.state[j][i + j] != '-') {
                    line = line.append(getStringPositionColorized(j, i + j));
                    nbSpace--;
                }
            }
            if (!line.toString().equals("")) {
                for (int k = 0; k < nbSpace * 2; k++) {
                    System.out.print(" ");
                }
                System.out.println(line);
            }
        }
    }

    /**
     * Retourne une châine de caractères correspondant à la postion sous la forme
     * d'une lettre et d'un chiffre accolé et mis en couleur en fonction de si la
     * case est occupée par le joueur bleu, rouge ou si elle est innocupée
     * @param i la ligne sur laquelle se situe la case dans state
     * @param j la colonne dans laquelle se situe la case dans state
     * @return une chaîne de caractère correspondant à la postion de la case
     */
    String getStringPositionColorized(int i, int j) {
        switch (this.state[i][j]) {
            case '.':
                return "" + ConsoleColors.BLACK + ConsoleColors.WHITE_BACKGROUND + intToChar(i) + j + ConsoleColors.RESET + "  ";
            case 'B':
                return "" + ConsoleColors.BLUE_BACKGROUND + intToChar(i) + j + ConsoleColors.RESET + "  ";
            case 'R':
                return "" + ConsoleColors.RED_BACKGROUND + intToChar(i) + j + ConsoleColors.RESET + "  ";
            default:
                return "";
        }
    }

    /**
     * Affiche le plateau de jeu dans la configuration portée par
     * l'attribut d'état "state" dans une interface graphique créé
     * à partir de StdDraw
     */
    void affiche2() {
        for(int i = this.state[0].length - 1; i > (-(this.state[0].length)); i--) {
            for(int j = 0; j < this.state.length; j++) {
                if((i + j) >= 0 && (i + j) < this.state[0].length && this.state[j][i+j] != '-') {
                    double[] position = getPieceCenter(j, i+j);
                    drawHexagon(position[0], position[1]);
                    drawCircle(position[0], position[1], PIECE_RADIUS, this.state[j][j+i]);
                    drawLabel(j, i+j, this.state[j][j+i]);
                }
            }
        }
    }


    /**
     * Initialise la fenêtre de dans laquelle le jeu sera affiché
     */
    void initWindow() {
        StdDraw.setCanvasSize(700, 700);
        StdDraw.setScale(-SIZE/2, SIZE/2);
    }

    /**
     * Retourne les coordonnées du centre de la pièce en fonction de sa position
     * dans le tableau state
     * @param idRow la ligne correspond à la position de la pièce dans le tableau state
     * @param idCol la colonne correspond à la position de la pièce dans le tableau state
     * @return un tableau de deux double avec en 0 la postion en x dans la fenêtre et en 1
     * la position en y dans la fenêtre
     */
    double[] getPieceCenter(int idRow, int idCol) {
        double[] position = new double[2];
        double radius = HEXAGON_RADIUS;
        position[0] = (idRow-3.0)*1.5*radius+(idCol-4.0)*1.5*radius;
        position[1] = (idRow-3.0)*(-Math.sqrt(3)*radius/2)+(idCol-4.0)*(Math.sqrt(3)*radius/2);

        return position;
    }

    /**
     * Dessine avec StdDraw un hexagone correspondant à une case existante du tableau state
     * @param x la position horizontale de la case à représenter
     * @param y la position verticale de la case à représenter
     */
    void drawHexagon(double x, double y) {
        StdDraw.setPenColor(StdDraw.BLACK);
        StdDraw.setPenRadius(0.0025);
        for(int i = 0; i < 6; i++) {
            double x1 = HEXAGON_RADIUS*Math.cos((2*Math.PI*i)/6)+x;
            double y1 = HEXAGON_RADIUS*Math.sin((2*Math.PI*i)/6)+y;
            double x2 = HEXAGON_RADIUS*Math.cos((2*Math.PI*(i+1))/6)+x;
            double y2 = HEXAGON_RADIUS*Math.sin((2*Math.PI*(i+1))/6)+y;
            StdDraw.line(x1, y1, x2, y2);
        }
    }

    /**
     * Dessine un cercle de couleur dans l'interface graphique à une position
     * de centre (x, y)
     *
     * @param x      Réel représentant la coordonnée x du centre du cercle
     * @param y      Réel représentant la coordonnée y du centre du cercle
     * @param radius Réel représentant le rayon du cercle
     * @param color  Couleur de remplissage du cercle
     */
    void drawCircle(double x, double y, double radius, char color) {
        switch (color) {
            case 'B':
                StdDraw.setPenColor(StdDraw.BLUE);
                break;
            case 'R':
                StdDraw.setPenColor(StdDraw.RED);
                break;
            default:
                StdDraw.setPenColor(StdDraw.WHITE);
                break;
        }
        StdDraw.filledCircle(x, y, radius);
    }

    /**
     * Dessine grâce à StdDraw le label correspond à une case au centre de cette case (ex: A4)
     * @param row la ligne à laquelle se trouve la case cherchée dans le tableau state
     * @param col la colonne à laquelle se trouve la case cherchée dans le tableau state
     * @param color la couleur de la pièce
     */
    void drawLabel(int row, int col, char color) {
        double[] position = getPieceCenter(row, col);
        if (color == 'B' || color == 'R') {
            StdDraw.setPenColor(StdDraw.WHITE);
        } else {
            StdDraw.setPenColor(StdDraw.BLACK);
        }
        StdDraw.text(position[0], position[1], "" + intToChar(row) + col);
    }

    /**
     * Affiche sur le plateau les informations utiles au bon déroulement du jeu.
     *
     * @param info
     */
    void drawLabelInformation(String info) {
        StdDraw.setPenColor(StdDraw.BLACK);
        StdDraw.text(0, BOARD_SIZE / 2.0, info);
    }

    /**
     * Récupère la pièce la plus proche des coordonnées (x, y)
     *
     * @param x
     * @param y
     * @return Chaine de caractères représentant la pièce.
     */
    String getNearestPoint(double x, double y) {
        String result = "A0";
        for(int i = this.state[0].length - 1; i > (-(this.state[0].length)); i--) {
            for(int j = 0; j < this.state.length; j++) {
                if((i + j) >= 0 && (i + j) < this.state[0].length && this.state[j][i+j] != '-') {
                    double[] position = getPieceCenter(j, i+j);
                    if(Math.sqrt(Math.pow((position[0] - x), 2) + Math.pow((position[1] - y), 2)) <= 0.35) {
                        result = "" + intToChar(j) + (i + j);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Joue un tour
     * @param couleur couleur du pion à jouer
     * @return tableau contenant la position de départ et la destination du pion à jouer.
     */
    String[] jouerIA(char couleur) {
        // votre code ici. Supprimer la ligne ci-dessous.
        throw new java.lang.UnsupportedOperationException("à compléter");
    }

    /**
     * Gère le jeu en fonction du joueur/couleur
     *
     * @param couleur
     * @return tableau de deux chaînes {source,destination} du pion à jouer
     */
    String[] jouer(char couleur) {
        String src = "";
        String dst = "";
        System.out.println("Mouvement " + couleur);

        src = displayMode == 1 ? input.next() : getSrc();
        dst = displayMode == 1 ? input.next() : getDest(src);
        System.out.println(src + "->" + dst);

        return new String[]{src, dst};
    }

    /**
     * Récupère la pièce source en fonction du clic initial du joueur.
     *
     * @return Chaine de caractères (pièce source)
     */
    String getSrc() {
        String src = "";
        double xMouse;
        double yMouse;

        while(src.equals("")) {
            if(StdDraw.isMousePressed()) {
                xMouse = StdDraw.mouseX();
                yMouse = StdDraw.mouseY();
                src = getNearestPoint(xMouse, yMouse);
            }
        }

        return src;
    }

    /**
     * Récupère la pièce de destination en fonction du relâchement du clic
     * Dessine la trace de la pièce prise en source
     *
     * @param src Pièce source (String)
     * @return Pièce destination (String)
     */
    String getDest(String src) {
        String dest = "";
        double xMouse;
        double yMouse;

        while(dest.equals("")) {
            StdDraw.clear();
            affiche2();
            xMouse = StdDraw.mouseX();
            yMouse = StdDraw.mouseY();
            if(!StdDraw.isMousePressed()) {
                dest = getNearestPoint(xMouse, yMouse);
            }
            int rowSrc = charToInt(src.charAt(0));
            int colSrc = Character.getNumericValue(src.charAt(1));
            double[] srcPos = getPieceCenter(rowSrc, colSrc);
            drawCircle(srcPos[0], srcPos[1], PIECE_RADIUS + 0.05, VIDE);
            drawLabel(rowSrc, colSrc, VIDE);
            if(this.state[rowSrc][colSrc] != VIDE) {
                drawCircle(xMouse, yMouse, PIECE_RADIUS, this.state[rowSrc][colSrc]);
            }
            StdDraw.show();
        }

        return dest;
    }

    /**
     * Retourne 'R' ou 'B' si vainqueur, 'N' si la partie n'est pas terminée.
     *
     * @param couleur
     * @return
     */
    char finPartie(char couleur){
        int nbPossibleMvtTotal = 0;
        for(int i = 0; i  < this.state.length; i++) {
            for (int j = 0; j < this.state[i].length; j++) {
                if(this.state[i][j] == couleur) {
                    nbPossibleMvtTotal += nbPossibleMvt(couleur, i, j);
                }
            }
        }

        return (nbPossibleMvtTotal == 0 ? couleur : 'N');
    }

    /**
     * Retourne le nombre de mouvements possibles en fonction de la pièce source.
     *
     * @param color Couleur du joueur actuel
     * @param idRow Identifiant de la ligne dans state
     * @param idCol Identifiant de la col. dans state
     * @return Nombre de mouvements possibles (int)
     */
    int nbPossibleMvt(char color, int idRow, int idCol) {
        int nbPossibleMvt = 0;
        String[] possibleDests;

        possibleDests = possibleDests(color, idRow, idCol);
        for (int k = 0; k < possibleDests.length; k++) {
            nbPossibleMvt += possibleDests[k].equals("") ? 0 : 1;
        }

        return nbPossibleMvt;
    }

    /**
     * Lance une partie en mode Console.
     *
     * @param jeu Instance de StuckWin
     */
    void gameTerminal(StuckWin jeu) {
        String src = "";
        String dest = "";
        String[] reponse;
        Result status;
        char partie;
        char curCouleur = jeu.joueurs[0];
        char nextCouleur = jeu.joueurs[1];
        char tmp;
        int cpt = 0;

        do {
            jeu.affiche();
            do {
                reponse = jeu.jouer(curCouleur);
                src = reponse[0];
                dest = reponse[1];
                if ("q".equals(src))
                    return;
                status = jeu.deplace(curCouleur, src, dest, ModeMvt.REAL);
                partie = jeu.finPartie(nextCouleur);
                System.out.println(statusStringGenerator(src, dest, status, partie));
                csvFileAppend(this.curCsvFile, curCouleur, src, dest, status);
            } while (status != Result.OK && partie == 'N');
            tmp = curCouleur;
            curCouleur = nextCouleur;
            nextCouleur = tmp;
            cpt++;
        } while (partie == 'N');

        System.out.println(victoryStringGenerator(partie, cpt));
    }

    /**
     * Lance une partie en mode Graphique via StdDraw.
     *
     * @param jeu Instance de StuckWin
     */
    void gameGUI(StuckWin jeu) {
        String src = "";
        String dest = "";
        String[] reponse;
        Result status;
        char partie;
        char curCouleur = jeu.joueurs[0];
        char nextCouleur = jeu.joueurs[1];
        char tmp;
        int cpt = 0;

        StdDraw.enableDoubleBuffering();
        jeu.initWindow();
        do {
            do {
                StdDraw.clear();
                jeu.affiche2();
                jeu.drawLabelInformation("Au tour de : " + (curCouleur == 'B' ? "Bleu" : "Rouge"));
                StdDraw.show();
                reponse = jeu.jouer(curCouleur);
                src = reponse[0];
                dest = reponse[1];
                status = jeu.deplace(curCouleur, src, dest, ModeMvt.REAL);
                partie = jeu.finPartie(nextCouleur);
                jeu.drawLabelInformation(statusStringGenerator(src, dest, status, partie));
                csvFileAppend(this.curCsvFile, curCouleur, src, dest, status);
                StdDraw.show();
                StdDraw.pause(500);
            } while (status != Result.OK && partie == 'N');
            tmp = curCouleur;
            curCouleur = nextCouleur;
            nextCouleur = tmp;
            cpt++;
        } while (partie == 'N');

        StdDraw.clear();
        jeu.affiche2();
        jeu.drawLabelInformation(victoryStringGenerator(partie, cpt));
        StdDraw.show();
    }

    /**
     * Sélectionne la méthode à appeler en fonction du mode de jeu choisi pour
     * lancer une partie.
     *
     * @param jeu         Instance de StuckWin
     * @param displayMode Mode de jeu (1: console, 2: graphique)
     */
    void runGame(StuckWin jeu, int displayMode) {
        this.displayMode = displayMode;
        if (displayMode == 1) {
            gameTerminal(jeu);
        } else {
            gameGUI(jeu);
        }
    }

    /**
     * Initialise un nouveau fichier trace (.csv) avec entêtes et commentaires.
     *
     * @param displayMode Mode de jeu (1: console, 2: graphique)
     */
    void initCsvFile(int displayMode) {
        //get all files in the current directory
        File[] files = new File(".").listFiles();
        //filter the files to only get the csv files
        File[] csvFiles = Arrays.stream(files).filter(f -> f.getName().endsWith(".csv")).toArray(File[]::new);
        int maxNum = 0;
        // match the file name with the pattern to get higher number
        for (File f : csvFiles) {
            Matcher m = Pattern.compile("StuckWin_(\\d+)\\.csv").matcher(f.getName());
            if (m.matches()) {
                maxNum = Math.max(maxNum, Integer.parseInt(m.group(1)));
            }
        }
        String filename;

        if (maxNum > 8) {
            filename = ("StuckWin_" + (maxNum + 1) + ".csv");
        } else {
            filename = ("StuckWin_0" + (maxNum + 1) + ".csv");
        }

        this.curCsvFile = new File(filename);
        System.out.println("Le jeu est lancé ! Vous trouverez la trace de cette partie dans le fichier " + filename);


        try (FileWriter curCsvFileEditor = new FileWriter(this.curCsvFile, true)) {
            String gameTypeStr = "";
            switch (displayMode) {
                case 1:
                    gameTypeStr = "Terminal";
                    break;
                case 2:
                    gameTypeStr = "Graphical";
                    break;
                default:
                    break;
            }
            curCsvFileEditor.append("# StuckWin Game\n");
            curCsvFileEditor.append("# Nathan BOSCHI, Jessy MOUGAMMADALY / Groupe 29\n");
            curCsvFileEditor.append("# Game played with " + gameTypeStr + " interface (" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + ")\n");
            curCsvFileEditor.append("color,start,dest,result\n");
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    /**
     * Ajoute une ligne au fichier trace (.csv) de la partie actuelle.
     *
     * @param file   Fichier à écrire
     * @param color  Couleur du joueur actuel (char)
     * @param start  Pièce source (String)
     * @param dest   Pièce dest. (String)
     * @param result Etat du mouvement
     */
    void csvFileAppend(File file, char color, String start, String dest, Result result) {
        try (FileWriter curCsvFileEditor = new FileWriter(file, true)) {
            curCsvFileEditor.append("" + color + "," + start + "," + dest + "," + result + "\n");
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    /**
     * Convertit un fichier en liste de tableaux.
     * Chaque ligne se transforme en String[] et est ajoutée à la liste.
     *
     * @param file Fichier à convertir
     * @return List<String [ ]>
     */
    List<String[]> csvToArray(File file) {
        if (file.exists() && file.getName().startsWith("StuckWin")) {
            try (Scanner fileScanner = new Scanner(file);) {
                List<String[]> csvArray = new ArrayList<>();
                while (fileScanner.hasNextLine()) {
                    String line = fileScanner.nextLine();
                    if (line.equals("color,start,dest,result") || line.startsWith("#"))
                        continue;
                    csvArray.add(line.split(","));
                }
                return csvArray;
            } catch (Exception e) {
                System.out.println(e);
                return Collections.emptyList();
            }
        } else {
            System.out.println("E : Le fichier entré en paramètre est inexistant ou n'est pas un fichier de trace StuckWin.");
            return Collections.emptyList();
        }
    }

    /**
     * Joue un fichier trace dans l'interface graphique.
     *
     * @param file Fichier trace à jouer (StuckWin_XX.csv)
     */
    void csvFilePlay(File file) {
        StdDraw.enableDoubleBuffering();
        initWindow();

        List<String[]> turnList = csvToArray(file);
        int cpt = 0;
        char partie = 'N';
        Result status = Result.OK;

        for (String[] turn : turnList) {
            StdDraw.clear();
            drawLabelInformation(statusStringGenerator(turn[1], turn[2], status, partie));
            affiche2();
            StdDraw.pause(10);
            StdDraw.show();
            status = deplace(turn[0].charAt(0), turn[1], turn[2], ModeMvt.REAL);
            if (status == Result.OK) {
                cpt++;
            }
        }

        partie = turnList.get(turnList.size() - 1)[0].charAt(0);
        partie = (partie == 'B') ? 'R' : 'B';
        StdDraw.clear();
        drawLabelInformation(victoryStringGenerator(partie, cpt));
        affiche2();
        StdDraw.show();
    }

    /**
     * Crée un String contenant l'état de la partie.
     *
     * @param src    Case source
     * @param dest   Case dest.
     * @param status Status déplacement
     * @param partie Status partie (N, B, R)
     * @return String destiné à être affichée.
     */
    String statusStringGenerator(String src, String dest, Result status, char partie) {
        return src + " -> " + dest + " / Statut : " + status + ", Etat de la Partie : " + partie;
    }

    /**
     * Crée un String contenant l'état final de la partie.
     *
     * @param partie Status partie (N, B, R)
     * @param cmpt   Nombre de coups
     * @return String destiné à être affichée.
     */
    String victoryStringGenerator(char partie, int cmpt) {
        return "Victoire : " + partie + " (" + (cmpt / 2) + " coups)";
    }

    /**
     * Affiche l'aide du jeu.
     */
    void printGameHelp() {
        System.out.println("StuckWin Game");
        System.out.println("java StuckWin [gameMode] [csvFile] :");
        System.out.println("\t-> [gameMode] (1: Console, 2: Graphical, 3: CSV File Playing, default: Graphical)");
        System.out.println("\t-> [csvFile] (si gameMode = 3) Trace à jouer au format 'StuckWin_XX.csv'");
    }


    public static void main(String[] args) {
        StuckWin jeu = new StuckWin();
        String arg = args.length > 0 ? args[0] : "2";
        if (arg.equals("--help")) {
            jeu.printGameHelp();
        } else {
            switch (Integer.parseInt(arg)) {
                case 1:
                    jeu.initCsvFile(1);
                    jeu.runGame(jeu, 1);
                    break;
                case 2:
                    jeu.initCsvFile(2);
                    jeu.runGame(jeu, 2);
                    break;
                case 3:
                    if (!args[1].isBlank()) {
                        File fileToPlay = new File(args[1]);
                        jeu.csvFilePlay(fileToPlay);
                    }
                    break;
                default:
                    break;
            }
        }
    }
}