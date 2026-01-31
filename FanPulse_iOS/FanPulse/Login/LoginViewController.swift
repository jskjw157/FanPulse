//
//  LoginViewController.swift
//  FanPulse
//
//  Created by 김송 on 12/28/25.
//

import UIKit
import SnapKit
import GoogleSignIn

class LoginViewController: UIViewController {
    
    private lazy var backgroundGradientView: FadeToBackgroundGradientView = {
        let view = FadeToBackgroundGradientView(
            baseColor: UIColor(hex: "#EC4899"),
            fadeColor: UIColor(hex: "#7E22CE"),
            startPoint: CGPoint(x: 0, y: 0),
            endPoint: CGPoint(x: 1, y: 1)
        )
        return view
    }()
    
    private let containerView: UIView = {
        let view = UIView()
        view.backgroundColor = .clear
        return view
    }()
    
    private let titleLabel: UILabel = {
        let label = UILabel()
        label.text = "Welcome to FanPulse"
        label.font = .systemFont(ofSize: 30, weight: .bold)
        label.textColor = .white
        label.textAlignment = .center
        return label
    }()
    
    private let subtitleLabel: UILabel = {
        let label = UILabel()
        label.text = "글로벌 K-POP 팬들의 인터랙티브 플랫폼"
        label.font = .systemFont(ofSize: 14)
        label.textColor = .white.withAlphaComponent(0.9)
        label.textAlignment = .center
        return label
    }()
    
    private lazy var googleSignUpButton: UIButton = {
        let button = UIButton()
        button.backgroundColor = .white
        button.layer.cornerRadius = 25
        button.layer.borderWidth = 1
        button.layer.borderColor = UIColor.gray.withAlphaComponent(0.3).cgColor
        
        let label = UILabel()
        label.text = "Google로 로그인"
        label.font = .systemFont(ofSize: 15, weight: .medium)
        label.textColor = .black
        button.addSubview(label)
        label.snp.makeConstraints { make in
            make.centerX.equalToSuperview().offset(16)
            make.centerY.equalToSuperview()
        }
        
        let imageView = UIImageView()
        imageView.image = UIImage(named: "logo_google")
        imageView.contentMode = .scaleAspectFit
        button.addSubview(imageView)
        imageView.snp.makeConstraints { make in
            make.trailing.equalTo(label.snp.leading).offset(-12)
            make.centerY.equalToSuperview()
            make.width.height.equalTo(20)
        }
        
        button.addTarget(self, action: #selector(handleGoogleSignIn), for: .touchUpInside)
        return button
    }()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setupUI()
    }
    
    private func setupUI() {
        view.backgroundColor = .white
        
        view.addSubview(backgroundGradientView)
        view.addSubview(containerView)
        
        containerView.addSubview(titleLabel)
        containerView.addSubview(subtitleLabel)
        containerView.addSubview(googleSignUpButton)
        
        backgroundGradientView.snp.makeConstraints { make in
            make.edges.equalToSuperview()
        }
        
        containerView.snp.makeConstraints { make in
            make.center.equalToSuperview()
        }
        
        titleLabel.snp.makeConstraints { make in
            make.top.equalToSuperview()
            make.horizontalEdges.equalTo(containerView).inset(24)
        }
        
        subtitleLabel.snp.makeConstraints { make in
            make.top.equalTo(titleLabel.snp.bottom).offset(8)
            make.leading.trailing.equalToSuperview().inset(24)
        }
        
        googleSignUpButton.snp.makeConstraints { make in
            make.top.equalTo(subtitleLabel.snp.bottom).offset(32)
            make.leading.trailing.equalToSuperview().inset(24)
            make.height.equalTo(49)
            make.bottom.equalToSuperview()
        }
    }
    
    // Google 로그인 처리
    @objc private func handleGoogleSignIn() {
        guard let clientID = Bundle.main.object(forInfoDictionaryKey: "GIDClientID") as? String else {
            showError(message: "Client ID를 찾을 수 없습니다")
            return
        }
        
        let config = GIDConfiguration(clientID: clientID)
        GIDSignIn.sharedInstance.configuration = config
        
        GIDSignIn.sharedInstance.signIn(withPresenting: self) { [weak self] result, error in
            guard let self = self else { return }
            
            if let error = error {
                self.showError(message: "로그인 실패: \(error.localizedDescription)")
                return
            }
            
            guard let user = result?.user,
                  let _ = user.idToken?.tokenString else {
                self.showError(message: "사용자 정보를 가져올 수 없습니다")
                return
            }
            
            let accessToken = user.accessToken.tokenString
            let refreshToken = user.refreshToken.tokenString
            let userID = user.userID ?? ""
            
            // 키체인에 저장
            KeychainManager.shared.saveToken(
                accessToken: accessToken,
                refreshToken: refreshToken,
                userID: userID
            )
            
            print("✅ 로그인 성공")
            print("이름: \(user.profile?.name ?? "")")
            print("이메일: \(user.profile?.email ?? "")")
            
            // 메인 화면으로 이동
            self.navigateToMainScreen()
        }
    }
    
    // 메인 화면으로 이동
    private func navigateToMainScreen() {
        let mainVC = MainTabBarController()
        
        if let window = self.view.window {
            UIView.transition(with: window, duration: 0.3, options: .transitionCrossDissolve) {
                window.rootViewController = mainVC
            }
        }
    }
    
    // 에러 표시
    private func showError(message: String) {
        let alert = UIAlertController(title: "오류", message: message, preferredStyle: .alert)
        alert.addAction(UIAlertAction(title: "확인", style: .default))
        present(alert, animated: true)
    }
}
