import dnl.utils.text.table.TextTable;

import java.util.ArrayList;

public class Utils {
    public static String divider = "=============";

    public static void printDivider(String text){
        System.out.println(divider + text + divider);
    }

    public static void printInfo(String text){
        System.out.println("** " + text);
    }

    public static void printError(String text, String err){
        System.out.println(">>>> " + text + err);
    }

    public static void printMenu(String headerText, String[] options){
        Utils.printDivider(headerText);
        System.out.println("Please select an option from below: ");
        int i = 1;
        for (String option : options){
            System.out.printf("\t %d -> %s%n", i, option);
            i+=1;
        }
        System.out.println("\t q -> exit");
        System.out.print("Choose an option: ");
    }

    public static void clearConsole() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public static void printTable(String[] columnNames, ArrayList<ArrayList<Object>> data){
        Object[][] objectArray = data.stream().map(u -> u.toArray(new Object[0])).toArray(Object[][]::new);
        TextTable tt = new TextTable(columnNames, objectArray);
        tt.printTable();
    }
}
