package shop.goodcasting.api.article.profile.service;

import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import shop.goodcasting.api.article.profile.domain.Profile;
import shop.goodcasting.api.article.profile.domain.ProfileDTO;
import shop.goodcasting.api.article.profile.repository.ProfileRepository;

import shop.goodcasting.api.file.domain.FileDTO;
import shop.goodcasting.api.file.domain.FileVO;
import shop.goodcasting.api.file.repository.FileRepository;
import shop.goodcasting.api.file.service.FileService;
import shop.goodcasting.api.user.actor.domain.Actor;
import shop.goodcasting.api.user.actor.domain.ActorDTO;
import shop.goodcasting.api.user.actor.repository.ActorRepository;
import shop.goodcasting.api.user.actor.service.ActorService;
import shop.goodcasting.api.user.login.repository.UserRepository;
import shop.goodcasting.api.user.login.service.UserService;

import javax.transaction.Transactional;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Lazy
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final ProfileRepository profileRepo;
    private final FileRepository fileRepo;
    private final FileService fileService;
    private final ActorService actorService;
    private final UserRepository userRepo;
    private final ActorRepository actorRepo;
    private final UserService userService;;



    @Transactional
    @Override
    public Long register(ProfileDTO profileDTO) {
        ProfileDTO finalProfileDto = entity2DtoAll(profileRepo.save(dto2EntityAll(profileDTO)));

        List<FileDTO> files = profileDTO.getFiles();

        if(files != null && files.size() > 0) {
            files.forEach(fileDTO -> {
                fileDTO.setProfile(finalProfileDto);
                System.out.println("----------------------after set final dto-----------------------: " + fileDTO);
                FileVO file = fileService.dto2EntityAll(fileDTO);
                System.out.println("----------------------after dto to entity-----------------------: " + file);

                fileRepo.save(file);
                if (file.isPhotoType()) {
                    extractCelebrity(file.getFileName(), finalProfileDto.getProfileId());
                }
            });
        }


        return null;
    }

    @Transactional
    @Override
    public ProfileDTO readProfile(Long profileId) {
        System.out.println("getProfileWithFileByProfileId() entry");

        List<Object[]> profileAndFileAndActor = profileRepo.getProfileAndFileAndActorByProfileId(2L);

        Profile profile = (Profile) profileAndFileAndActor.get(0)[0];
        Actor actor = profile.getActor();
        System.out.println("actor: " + actor);

        ProfileDTO profileDTO = entity2Dto(profile);
        System.out.println("profileDTO: " + profileDTO);

        ActorDTO actorDTO = actorService.entity2Dto(actor);
        System.out.println("actorDTO: " + actorDTO);

        List<FileDTO> fileList = new ArrayList<>();

        profileAndFileAndActor.forEach(objects -> {
            fileList.add(fileService.entity2Dto((FileVO)objects[2]));
        });

        profileDTO.setActor(actorDTO);
        profileDTO.setFiles(fileList);

        System.out.println("profile dto: " + profileDTO);

        return profileDTO;
    }

    public List<ProfileDTO> readProfileList() {


//        for (Object[] re : res) {
//            System.out.println("loop enter");
//            System.out.println(Arrays.toString(re));
//        }

        List<ProfileDTO> profileList = profileRepo.getProfileAndFileAndActorByFirst(true)
                .stream().map(objects -> {
                    System.out.println("loop enter");
                    System.out.println(Arrays.toString(objects));

                    ProfileDTO profileDTO = entity2Dto((Profile) objects[0]);
                    ActorDTO actorDTO = actorService.entity2Dto((Actor) objects[1]);
                    FileDTO fileDTO = fileService.entity2Dto((FileVO) objects[2]);

                    List<FileDTO> files = new ArrayList<>();
                    files.add(fileDTO);

                    profileDTO.setActor(actorDTO);
                    profileDTO.setFiles(files);

                    System.out.println(profileDTO);

                    return profileDTO;
                }).collect(Collectors.toList());

        for (ProfileDTO profileDTO : profileList) {
            System.out.println("----------------------------------------");
            System.out.println(profileDTO.getTitle());
            System.out.println(profileDTO.getActor());
            System.out.println(profileDTO.getFiles());
        }

        return profileList;
    }


    public void extractCelebrity(String photoName, Long profileId) {

        StringBuffer reqStr = new StringBuffer();
        String clientId = "92mep69l88";//애플리케이션 클라이언트 아이디값";
        String clientSecret = "qdbpwHd8pRZPszLr0gLfqKR7OHbdsDriRmOFdwno";//애플리케이션 클라이언트 시크릿값";

        try {
            String paramName = "image"; // 파라미터명은 image로 지정
            String imgFile = photoName;
            File uploadFile = new File(imgFile);
            String apiURL = "https://naveropenapi.apigw.ntruss.com/vision/v1/celebrity"; // 유명인 얼굴 인식
            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setUseCaches(false);
            con.setDoOutput(true);
            con.setDoInput(true);
            // multipart request
            String boundary = "---" + System.currentTimeMillis() + "---";
            con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            con.setRequestProperty("X-NCP-APIGW-API-KEY-ID", clientId);
            con.setRequestProperty("X-NCP-APIGW-API-KEY", clientSecret);
            OutputStream outputStream = con.getOutputStream();
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"), true);
            String LINE_FEED = "\r\n";
            // file 추가
            String fileName = uploadFile.getName();
            writer.append("--" + boundary).append(LINE_FEED);
            writer.append("Content-Disposition: form-data; name=\"" + paramName + "\"; filename=\"" + fileName + "\"").append(LINE_FEED);
            writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(fileName)).append(LINE_FEED);
            writer.append(LINE_FEED);
            writer.flush();
            FileInputStream inputStream = new FileInputStream(uploadFile);
            byte[] buffer = new byte[4096];
            int bytesRead = -1;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
            inputStream.close();
            writer.append(LINE_FEED).flush();
            writer.append("--" + boundary + "--").append(LINE_FEED);
            writer.close();
            BufferedReader br = null;
            int responseCode = con.getResponseCode();
            if (responseCode == 200) { // 정상 호출
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else {  // 오류 발생
                System.out.println("error!!!!!!! responseCode= " + responseCode);
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            }
            String inputLine;
            if (br != null) {
                StringBuffer response = new StringBuffer();
                while ((inputLine = br.readLine()) != null) {
                    response.append(inputLine);
                }
                br.close();
                System.out.println("response: " + response.toString());

                JSONObject jsonObject = new JSONObject(response.toString());
                JSONArray facesArr = jsonObject.getJSONArray("faces");
                System.out.println("---------------------facesArr-----------------" + facesArr);

                JSONObject elem = facesArr.getJSONObject(0);
                System.out.println("---------------------elem-----------------" + elem);

                JSONObject celebObject = elem.getJSONObject("celebrity");
                System.out.println("---------------------celebObject-----------------" + celebObject);

                String resemble = celebObject.getString("value");
                System.out.println("---------------------resemble-----------------" + resemble);

                String confidence = String.valueOf(celebObject.getFloat("confidence"));
                System.out.println("---------------------confidence-----------------" + confidence);

                System.out.println("===================================================================");

                profileRepo.resembleUpdate(profileId, resemble, confidence);

            } else {
                System.out.println("error !!!");
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}

