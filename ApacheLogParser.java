package apachelogparser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Parth
 */

interface LogExample {
  /** The number of fields that must be found. */
  public static final int NUM_FIELDS = 9;

  /** The sample log entry to be parsed. */
  public static final String logEntryLine = "123.45.67.89 - - [27/Oct/2000:09:27:09 -0400] \"GET /java/javaResources.html HTTP/1.0\" 200 10450 \"-\" \"Mozilla/4.6 [en] (X11; U; OpenBSD 2.8 i386; Nav)\"";

}

public class ApacheLogParser implements LogExample{

    /**
     * @param args the command line arguments
     */
    public static String LogFolderPath="E:\\Software setups\\apache-tomcat-7.0.59\\logs\\";
    public static String accessLogPrefix="localhost_access_log";
    public static String accessLogFileExt=".txt";
    
    public static void main(String[] args) throws FileNotFoundException, IOException, SQLException {
        String logEntryPatternCombinedLogFormat = "^([\\d.]+) (\\S+) (\\S+) \\[([\\w:/]+\\s[+\\-]\\d{4})\\] \"(.+?)\" (\\d{3}) (\\d+) \"([^\"]+)\" \"([^\"]+)\"";
        String logEntryPatternCLF = "^(\\S+) (\\S+) (\\S+) \\[(.*?)\\] \"(.*?)\" (\\S+) (\\S+)( \"(.*?)\" \"(.*?)\")?";
        
        String logEntry;
        
        if(args[0]==null){
            System.err.println("Enter log folder path in args");
            System.exit(1);
        }
        String logFolderPathArg=args[0];
        
        Connection con;
        String conString="jdbc:odbc:" +
                        "Driver={Microsoft Excel Driver (*.xls, *.xlsx, *.xlsm, *.xlsb)};" +
                        "DBQ=C:\\Users\\Parth\\Documents\\NetBeansProjects\\Databases\\ApacheTomcatAccessLogs.xlsx;" +
                        "ReadOnly=0;";
        PreparedStatement ps;
        String insertQuery="INSERT INTO [SHEET1$] VALUES (?,?,?,?,?)";
        
        System.out.println("Using RE Pattern:");
        System.out.println(logEntryPatternCLF);

//        System.out.println("Input line is:");
//        System.out.println(logEntry);

        Pattern p = Pattern.compile(logEntryPatternCLF);
        File logFolder=new File(logFolderPathArg);
        
        if(logFolder.isDirectory()){
            con = DriverManager.getConnection(conString);
            ps = con.prepareStatement(insertQuery);
            
            String[] logFileList=logFolder.list();
            
            for(String lf : logFileList){
                if(lf.startsWith(accessLogPrefix)){
                    BufferedReader br=new BufferedReader(new FileReader(logFolderPathArg+lf));
        
                    logEntry=br.readLine();
                    
                    while(logEntry!=null){
                        if(logEntry.equals(""))break;
                        Matcher matcher = p.matcher(logEntry);
                        if (!matcher.matches()) {
                          System.err.println("Bad log entry (or problem with RE?):");
                          System.err.println(logEntry);
                          return;
                        }

                        ps.setString(1, matcher.group(1));
                        ps.setString(2, matcher.group(4));
                        ps.setString(3, matcher.group(5));
                        ps.setString(4, matcher.group(6));
                        ps.setString(5, matcher.group(7));
                        ps.execute();
                        logEntry=br.readLine();
                    }
                     br.close();
                }
            }
            con.close();
            ps.close();  
        }
    }
}
