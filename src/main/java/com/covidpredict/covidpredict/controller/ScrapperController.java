package com.covidpredict.covidpredict.controller;

import com.covidpredict.covidpredict.model.Total;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVWriter;
import org.apache.commons.io.IOUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbookFactory;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/")
public class ScrapperController {

    private static final ObjectMapper objectMapper =
            new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    //@Value("${car.srcUrl}")
    private static String srcUrlFirstHalf = "https://api.covid19tracker.in/data/static/timeseries/";
    private static String srcUrlSecondHalf = ".min.json";

    private static Map<String,String> stateMap = new HashMap<String, String>() {{
        put("AS", "assam");
        put("AN", "andaman");
        put("AP", "andhrapradesh");
        put("AR", "arunachalpradesh");
        put("BR", "bihar");
        put("CH", "chandigarh");
        put("CT", "chattisgarh");
        put("DN", "dadra");
        put("DL", "delhi");
        put("GA", "goa");
        put("GJ", "Gujarat");
        put("HR", "Haryana");
        put("HP", "Himachal");
        put("JK", "Jammu");
        put("JH", "Jharkhand");
        put("KA", "Karnataka");
        put("KL", "Kerala");
        put("LD", "Lakshadweep");
        put("MP", "MadhyaPradesh");
        put("MH", "Maharashtra");
        put("MN", "Manipur");
        put("ML", "Meghalaya");
        put("MZ", "Mizoram");
        put("NL", "Nagaland");
        put("OR", "Odisha");
        put("PY", "Puducherry");
        put("PB", "Punjab");
        put("RJ", "Rajasthan");
        put("SK", "Sikkim");
        put("TN", "TamilNadu");
        put("TG", "Telangana");
        put("TR", "Tripura");
        put("UP", "UttarPradesh");
        put("UT", "Uttarakhand");
        put("WB", "WestBengal");
    }};



    /*@GetMapping("covidpredict")
    ResponseEntity<String> covidpredict(@RequestParam String localDir) throws IOException {
        {
            //get units for each state
            stateMap.forEach((stateCode, stateDesc) -> {
                try {
                    System.out.println(stateDesc + " ");
                    List<String[]> dataList = getDataToBeAppended(stateCode, stateDesc);
                    File fileName = getFileNameFromlocalRepo(localDir, stateDesc);
                    try (CSVWriter writer = new CSVWriter(new FileWriter(fileName, true))) {
                        writer.writeAll(dataList);
                    }
                    System.out.println(stateDesc + " is completed");
                }catch(Exception e){
                    e.printStackTrace();
                }
            });
        }
        return new ResponseEntity<String>("Success", HttpStatus.OK);
    }*/

    public static void main(String[] args) throws IOException, JSONException/*, GitAPIException*/ {
        stateMap.forEach((stateCode, stateDesc) -> {
            try {

                List<String[]> dataList = getDataToBeAppended(stateCode, stateDesc);
                File fileName = getFileNameFromlocalRepo(args[0], stateDesc);
                try (CSVWriter writer = new CSVWriter(new FileWriter(fileName))) {
                    writer.writeAll(dataList);
               }
                System.out.println(stateDesc + " is completed");
            }catch(Exception e){
                e.printStackTrace();
            }
        });



    }

    private static File getFileNameFromlocalRepo(String s1, String s) {
        return new File(s1 + "//COVIDPredictDeploy//data_" + s + ".csv");
    }

    /*private static File getFileNameFromlocalRepo(String location) *//*throws GitAPIException*//* {
        // Monitor to get git command progress printed on java System.out console
        TextProgressMonitor consoleProgressMonitor = new TextProgressMonitor(new PrintWriter(System.out));

        // Local directory on this machine where we will clone remote repo.
        File localRepoDir = new File(location);
        Repository repo = Git.cloneRepository().setProgressMonitor(consoleProgressMonitor).setDirectory(localRepoDir)
                .setURI("https://github.com/AKKwork/COVIDPredictDeploy.git").call().getRepository();
        try (Git git = new Git(repo)) {
            BaseLocalApplicationProperties master;
            if(Arrays.asList(localRepoDir.list()).contains("COVIDPredictDeploy")) {
                git.checkout().setProgressMonitor(consoleProgressMonitor).setCreateBranch(true).setName("local-master")
                        .setStartPoint("master").call();
            }
            localRepoDir = new File(location + "//COVIDPredictDeploy");
            return Arrays.stream(localRepoDir.listFiles())
                    .filter(f -> f.getName().contains("data_assam.csv")).findFirst().get();

        }
    }*/

    /**
     * Method which gives the data to be appended in the CSV
     * @return
     * @throws JSONException
     * @throws IOException
     * @param stateCode
     * @param stateDesc
     */
    private static List<String[]> getDataToBeAppended(String stateCode, String stateDesc) throws JSONException, IOException {

        String[] header = {"state", "district", "date", "new_diagnoses", "new_tests", "new_deaths"};
        JSONObject json = new JSONObject(IOUtils.toString(new URL(srcUrlFirstHalf + stateCode + srcUrlSecondHalf), StandardCharsets.UTF_8));
        List<String[]> dataList = new ArrayList<>();
        JSONObject districts = (JSONObject) json.getJSONObject(stateCode).get("districts");
        dataList.add(header);
        for(String district : districts.keySet()){
            JSONObject json1 = (JSONObject) districts.getJSONObject(district).get("dates");
            List<LocalDate> keySet = json1.keySet().stream().map(s ->
                    LocalDate.parse(s, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            ).collect(Collectors.toList());
            Collections.sort(keySet);
            List<LocalDate> keySetSubList = keySet.subList(keySet.size() > 91 ? keySet.size() - 91: 0, keySet.size());

            Total totalFirst = objectMapper.readValue(json1.getJSONObject( keySetSubList.get(0).toString() ).get("total").toString(), Total.class);
            Integer confirmedDateB4 = totalFirst.getConfirmed();
            Integer testedB4 = totalFirst.getTested();
            Integer deceasedB4 = totalFirst.getDeceased();
            keySetSubList.remove(0);
            for (LocalDate key : keySetSubList) {
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                Total total = objectMapper.readValue(json1.getJSONObject( key.toString() ).get("total").toString(), Total.class);

                String[] data = {stateDesc, district, String.valueOf(key), String.valueOf(total.getConfirmed() - confirmedDateB4),
                        String.valueOf(total.getTested() - testedB4),
                        String.valueOf(total.getDeceased() - deceasedB4)};
                confirmedDateB4 = total.getConfirmed();
                testedB4 = total.getTested();
                deceasedB4 = total.getDeceased();
                dataList.add(data);
            }
        }
        //State data
            JSONObject json1 = (JSONObject) json.getJSONObject(stateCode).get("dates");
            List<LocalDate> keySet = json1.keySet().stream().map(s ->
                    LocalDate.parse(s, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            ).collect(Collectors.toList());
            Collections.sort(keySet);
            List<LocalDate> keySetSubList = keySet.subList(keySet.size() > 91 ? keySet.size() - 91: 0, keySet.size());

            Total totalFirst = objectMapper.readValue(json1.getJSONObject( keySetSubList.get(0).toString() ).get("total").toString(), Total.class);
            Integer confirmedDateB4 = totalFirst.getConfirmed();
            Integer testedB4 = totalFirst.getTested();
            Integer deceasedB4 = totalFirst.getDeceased();
            keySetSubList.remove(0);
            for (LocalDate key : keySetSubList) {
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                Total total = objectMapper.readValue(json1.getJSONObject( key.toString() ).get("total").toString(), Total.class);

                String[] data = {stateDesc, stateDesc, String.valueOf(key), String.valueOf(total.getConfirmed() - confirmedDateB4),
                        String.valueOf(total.getTested() - testedB4),
                        String.valueOf(total.getDeceased() - deceasedB4)};
                confirmedDateB4 = total.getConfirmed();
                testedB4 = total.getTested();
                deceasedB4 = total.getDeceased();
                dataList.add(data);
            }
        //}

        return dataList;

    }

}
