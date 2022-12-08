import java.io.File;
import java.util.Scanner;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class StuckWin {
    static final Scanner input = new Scanner(System.in);
    private static final double BOARD_SIZE = 7;
    static final double PIECE_RADIUS = 0.35;
    static final double HEXAGON_RADIUS = 0.5;
    static final int DEFAULT_SPACE_NUMBER = 5;

    File curCsvFile;


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
     * Vérifie si la position passée en paramètre n'est pas dans les limites de la zones de jeu
     * @param position case dont la position va être testée
     * @return vrai si la case n'est pas dans le plateau de jeu, et faux si elle l'est
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
        int x = idCol+orientation*(int)Math.round(Math.cos(2*Math.PI*i/8.0+(Math.PI/2)));
        int y = idLettre+orientation*(int)Math.round(Math.sin(2*Math.PI*i/8.0+(Math.PI/2)));
        if(x >= 0 && y >= 0 && x < (BOARD_SIZE+1) && y < BOARD_SIZE && this.state[y][x] == VIDE) {
          result[i] = "" + intToChar(y) + x;
        }
      }

      return result;
    }

    void affiche() {
      for(int i = this.state[0].length - 1; i > (-(this.state[0].length)); i--) {
        StringBuilder line = new StringBuilder("");
        int nbSpace = DEFAULT_SPACE_NUMBER;
        for(int j = 0; j < this.state.length; j++) {
          if((i + j) >= 0 && (i + j) < this.state[0].length && this.state[j][i+j] != '-') {
            line = line.append(getStringPositionColorized(j, i+j));
            nbSpace--;
          }
        }
        if(!line.toString().equals("")) {
          for(int k = 0; k < nbSpace * 2; k++) {
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
     * l'attribut d'état "state"
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
     * 
     * @param x
     * @param y
     * @param radius
     * @param color
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
      if(color == 'B' || color == 'R') {
        StdDraw.setPenColor(StdDraw.WHITE);
      }else {
        StdDraw.setPenColor(StdDraw.BLACK);
      }
      StdDraw.text(position[0], position[1], "" + intToChar(row) + col);
    }

    void drawLabelInformation(String info) {
      StdDraw.setPenColor(StdDraw.BLACK);
      StdDraw.text(0, BOARD_SIZE/2.0, info);
    }

    /**
     * 
     * @param x
     * @param y
     * @return
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
     * gère le jeu en fonction du joueur/couleur
     * @param couleur
     * @return tableau de deux chaînes {source,destination} du pion à jouer
     */
    String[] jouer(char couleur){
      String src = "";
      String dst = "";
      System.out.println("Mouvement " + couleur);

      src = input.next();
      dst = input.next();
      System.out.println(src + "->" + dst);

      return new String[]{src, dst};
    }

    /**
     * gère le jeu en fonction du joueur/couleur
     * @param couleur
     * @return tableau de deux chaînes {source,destination} du pion à jouer
     */
    String[] jouer2(char couleur){
        String src = "";
        String dst = "";
        System.out.println("Mouvement " + couleur);

        src = getSrc();
        dst = getDest(src);
        System.out.println(src + "->" + dst);

        return new String[]{src, dst};
    }

    /**
     * 
     * @return
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
     * 
     * @param src
     * @return
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
     * retourne 'R' ou 'B' si vainqueur, 'N' si partie pas finie
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
     * 
     * @param color
     * @param idRow
     * @param idCol
     * @return
     */
    int nbPossibleMvt(char color, int idRow, int idCol) {
      int nbPossibleMvt = 0;
      String[] possibleDests;

      possibleDests = possibleDests(color, idRow, idCol);
      for(int k = 0; k < possibleDests.length; k++) {
        nbPossibleMvt += possibleDests[k].equals("") ? 0 : 1;
      }

      return nbPossibleMvt;
    }

    void game(StuckWin jeu) {
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
          if("q".equals(src))
            return;
          status = jeu.deplace(curCouleur, src, dest, ModeMvt.REAL);
          partie = jeu.finPartie(nextCouleur);
          System.out.println("status : "+status + " partie : " + partie);
        } while(status != Result.OK && partie=='N');
        tmp = curCouleur;
        curCouleur = nextCouleur;
        nextCouleur = tmp;
        cpt ++;
      } while(partie =='N');
      
      System.out.println("Victoire : " + partie + " (" + (cpt/2) + " coups)");
    }

    void game2(StuckWin jeu) {
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
              reponse = jeu.jouer2(curCouleur);
              src = reponse[0];
              dest = reponse[1];
              status = jeu.deplace(curCouleur, src, dest, ModeMvt.REAL);
              partie = jeu.finPartie(nextCouleur);
              jeu.drawLabelInformation("status : "+status + " partie : " + partie);
              StdDraw.show();
              StdDraw.pause(500);
            } while(status != Result.OK && partie=='N');
            tmp = curCouleur;
            curCouleur = nextCouleur;
            nextCouleur = tmp;
            cpt++;
          } while(partie =='N');
  
          StdDraw.clear();
          jeu.affiche2();
          jeu.drawLabelInformation("Victoire : " + partie + " (" + (cpt/2) + " coups)");
          StdDraw.show();
    }

    void initCsvFile(){
        //get all files in the current directory
        File[] files = new File(".").listFiles();
        //filter the files to only get the csv files
        File[] csvFiles = Arrays.stream(files).filter(f -> f.getName().endsWith(".csv")).toArray(File[]::new);
        int maxNum = 0;
        // match the file name with the pattern to get higher number
        for(File f : csvFiles){
            Matcher m = Pattern.compile("StuckWin_(\\d+)\\.csv").matcher(f.getName());
            if(m.matches()){
                maxNum = Math.max(maxNum, Integer.parseInt(m.group(1)));
            }
        }
        this.curCsvFile = new File("StuckWin_"+(maxNum+1)+".csv");
    }

    public static void main(String[] args) {
      StuckWin jeu = new StuckWin();

      switch (args[0]) {
        case "0":
          jeu.game(jeu);
          break;
        case "1":
            jeu.game2(jeu);
          break;
        default:
          break;
      }

    }
}
