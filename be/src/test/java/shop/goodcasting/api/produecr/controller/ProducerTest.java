package shop.goodcasting.api.produecr.controller;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import shop.goodcasting.api.user.login.domain.Role;
import shop.goodcasting.api.user.login.domain.UserVO;
import shop.goodcasting.api.user.login.repository.UserRepository;
import shop.goodcasting.api.user.producer.domain.Producer;
import shop.goodcasting.api.user.producer.repository.ProducerRepository;


import java.util.List;
import java.util.stream.IntStream;

@SpringBootTest
public class ProducerTest {

    @Autowired
    private ProducerRepository producerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void insertDummies(){
        IntStream.rangeClosed(1773, 1833).forEach(i -> {
            UserVO userVO = UserVO.builder()
                    .username("user" + i)
                    .position(false)
                    .password(  passwordEncoder.encode("1111") )
                    .build();
            userRepository.save(userVO);

            Producer producer = Producer.builder()
                    .email("producer" + i + "@daum.net")
                    .agency("소속사" + i)
                    .phone("대표번호" + i)
                    .position("직급" + i)
                    .userVO(userVO)
                    .build();
            producerRepository.save(producer);
        });
    }


}
