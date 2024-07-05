package gift.service;

import gift.controller.dto.ChangePasswordDTO;
import gift.controller.dto.TokenResponse;
import gift.controller.dto.UserDTO;
import gift.domain.UserInfo;
import gift.repository.UserInfoRepository;
import gift.utils.JwtTokenProvider;
import gift.utils.error.UserAlreadyExistsException;
import gift.utils.error.UserNotFoundException;
import gift.utils.error.UserPasswordNotFoundException;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserInfoRepository userInfoRepository;
    private final JwtTokenProvider jwtTokenProvider;


    public UserService(UserInfoRepository userInfoRepository, JwtTokenProvider jwtTokenProvider) {
        this.userInfoRepository = userInfoRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public TokenResponse registerUser(UserDTO userDTO) {
        UserInfo userInfo = new UserInfo(userDTO.getEmail(), userDTO.getPassword());
        Boolean result = userInfoRepository.save(userInfo);
        if (!result) {
            throw new UserAlreadyExistsException("User Already Exist");
        }
        return new TokenResponse(jwtTokenProvider.createToken(userDTO.getEmail()));
    }

    public TokenResponse login(UserDTO userDTO){
        UserInfo userInfo = new UserInfo(userDTO.getEmail(),userDTO.getPassword());
        Optional<UserInfo> byEmail = userInfoRepository.findByEmail(userDTO.getEmail());
        if (byEmail.isEmpty()){
            throw new UserNotFoundException("User NOT FOUND");
        }
        return new TokenResponse(jwtTokenProvider.createToken(userDTO.getEmail()));
    }

    public boolean changePasswordDTO(ChangePasswordDTO changePasswordDTO){
        UserInfo userInfo = userInfoRepository.findByEmail(changePasswordDTO.getEmail())
            .orElseThrow(() -> new UserNotFoundException("User not found"));
        if (!userInfo.getPassword().equals(changePasswordDTO.getCurrentPassword())){
            throw new UserPasswordNotFoundException("Password not found");
        }
        userInfo.setPassword(changePasswordDTO.getNewPassword());
        Boolean result = userInfoRepository.changePassword(userInfo);
        return true;
    }

    public UserDTO findPassword(String email){
        Optional<UserInfo> byEmail = userInfoRepository.findByEmail(email);
        if (byEmail.isEmpty()){
            throw new UserNotFoundException("User NOT FOUND");
        }
        return new UserDTO(byEmail.get().getPassword(),email);
    }


}
