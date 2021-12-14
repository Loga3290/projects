package com.covidpredict.covidpredict.controller;

import com.covidpredict.covidpredict.model.Total;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVWriter;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
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
        put("DD", "damandiu");
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



    @GetMapping("covidpredict")
    ResponseEntity<String> covidpredict(@RequestParam String localDir) throws IOException {
        {
            //get units for each state
            stateMap.forEach((stateCode, stateDesc) -> {
                try {
                    List<String[]> dataList = getDataToBeAppended(stateCode, stateDesc);
                    File fileName = getFileNameFromlocalRepo(localDir, stateDesc);
                    try (CSVWriter writer = new CSVWriter(new FileWriter(fileName, true))) {
                        writer.writeAll(dataList);
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            });
        }
        return new ResponseEntity<String>("Success", HttpStatus.OK);
    }

//    public static void main(String[] args) throws IOException, JSONException/*, GitAPIException*/ {
//        System.out.println("test");
//        //get units for each state
//        stateMap.forEach((stateCode, stateDesc) -> {
//            System.out.println("inside forEach " + stateMap.toString());
//            try {
//                List<String[]> dataList = getDataToBeAppended(stateCode, stateDesc);
//                // default all fields are enclosed in double quotes
//                // default separator is a comma
//                File fileName = getFileNameFromlocalRepo("C:\\Loga", stateDesc);
//                try (CSVWriter writer = new CSVWriter(new FileWriter(fileName, true))) {
//                    writer.writeAll(dataList);
//                }
//            }catch(Exception e){
//                //return new ResponseEntity<>(HttpStatus.OK);
//            }
//        });
//
//
//
//    }

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
        System.out.println("inside get Data to be appended");

        String[] header = {"date", "new_diagnoses", "new_tests", "new_deaths"};
        JSONObject json = new JSONObject(IOUtils.toString(new URL(srcUrlFirstHalf + stateCode + srcUrlSecondHalf), StandardCharsets.UTF_8));
        JSONObject json1 = (JSONObject) json.getJSONObject(stateCode).get("dates");
        List<LocalDate> keySet = json1.keySet().stream().map(s ->
                LocalDate.parse(s, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        ).collect(Collectors.toList());
        Collections.sort(keySet);
        List<LocalDate> keySetSubList = keySet.subList(keySet.size() - 91, keySet.size());
        List<String[]> dataList = new ArrayList<>();
        dataList.add(header);
        Total totalFirst = objectMapper.readValue(json1.getJSONObject( keySetSubList.get(0).toString() ).get("total").toString(), Total.class);
        Integer confirmedDateB4 = totalFirst.getConfirmed();
        Integer testedB4 = totalFirst.getTested();
        Integer deceasedB4 = totalFirst.getDeceased();
        keySetSubList.remove(0);
        for (LocalDate key : keySetSubList) {
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            Total total = objectMapper.readValue(json1.getJSONObject( key.toString() ).get("total").toString(), Total.class);
            //Meta meta = objectMapper.readValue(json.getJSONObject(key).get("meta").toString(), Meta.class);

            String[] data = {String.valueOf(key), String.valueOf(total.getConfirmed() - confirmedDateB4),
                    String.valueOf(total.getTested() - testedB4),
                    String.valueOf(total.getDeceased() - deceasedB4)};
            confirmedDateB4 = total.getConfirmed();
            testedB4 = total.getTested();
            deceasedB4 = total.getDeceased();
            dataList.add(data);
        }

        return dataList;

    }

}
