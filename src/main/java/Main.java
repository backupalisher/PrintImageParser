import java.io.*;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Main {
    private static final String path = "e:\\part4\\part4_spec\\";
    private static final String[] brand_list = {"brother","canon","epson","hp","konica minolta","kyocera","lexmark","oki","panasonic","ricoh","riso","samsung","sharp","toshiba","xerox"};
    private static FileOutputStream saveFileStream;

    public static void main(String[] args) {
        int brand_id = 0; // ID ПРОИЗВОДИТЕЛЯ
        String path_list = null;
        int fIndex;

        Date startTime = new Date();
        System.out.println("Запуск " + startTime.toString());

        try {
            saveFileStream = new FileOutputStream(path+"all_image_url", true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        for (int j = 0; j < brand_list.length; j++) {
            path_list = path + brand_list[j];
            System.out.println(j + 1 + ": " + path_list);

            try {
                File dir = new File(path_list); //path указывает на директорию
                File[] arrFiles = dir.listFiles();
                List<File> lst = Arrays.asList(arrFiles);

                System.out.println(lst.size()/2);
                fIndex = 0;

                for (File file : lst) {
                    try {
                        String fileName = file.getName();
                        fileName = fileName.replaceAll(".csv","");
                        if (fileName.contains("_images")){
                            fileName = fileName.replaceAll("_images","");

                            try {
                                FileInputStream fileInputStream = new FileInputStream(file.getAbsolutePath());
                                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
                                String strLine;
                                StringBuilder sb = new StringBuilder();

                                while ((strLine = bufferedReader.readLine()) != null) {
                                    strLine = strLine.replaceAll("https://static.1k.by","");
                                    sb.append(strLine).append(System.lineSeparator());
                                    saveFileStream.write(sb.toString().getBytes());
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }


            } catch (Exception e) {
                e.printStackTrace();
            }


        }
    }
}
