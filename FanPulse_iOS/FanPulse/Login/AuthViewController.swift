//
//  AuthViewController.swift
//  FanPulse
//
//  Created by 김송 on 12/28/25.
//

import UIKit
import SnapKit

class AuthViewController: UIViewController {
    
    private var isLoginSelected = true {
        didSet {
            updateTabSelection()
            updateContentView()
            updateCardHeight()
        }
    }
    
    private var cardHeightConstraint: Constraint?
    
    private lazy var backgroundGradientView: FadeToBackgroundGradientView = {
        let view = FadeToBackgroundGradientView(
            baseColor: UIColor(hex: "#EC4899"),
            fadeColor: UIColor(hex: "#7E22CE"),
            startPoint: CGPoint(x: 0, y: 0),
            endPoint: CGPoint(x: 1, y: 1)
        )
        return view
    }()
    
    private let titleLabel: UILabel = {
        let label = UILabel()
        label.text = "Welcome to FanPulse"
        label.font = .systemFont(ofSize: 28, weight: .bold)
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
    
    private let cardView: UIView = {
        let view = UIView()
        view.backgroundColor = .white
        view.layer.cornerRadius = 30
        view.clipsToBounds = true
        return view
    }()
    
    private let tabContainer: UIView = {
        let view = UIView()
        view.backgroundColor = UIColor.gray.withAlphaComponent(0.1)
        view.layer.cornerRadius = 22
        return view
    }()
    
    private lazy var loginTabButton: UIButton = {
        let button = UIButton()
        button.setTitle("로그인", for: .normal)
        button.titleLabel?.font = .systemFont(ofSize: 16, weight: .semibold)
        button.layer.cornerRadius = 18
        button.addTarget(self, action: #selector(loginTabTapped), for: .touchUpInside)
        return button
    }()
    
    private lazy var signUpTabButton: UIButton = {
        let button = UIButton()
        button.setTitle("회원가입", for: .normal)
        button.titleLabel?.font = .systemFont(ofSize: 16, weight: .semibold)
        button.layer.cornerRadius = 18
        button.addTarget(self, action: #selector(signUpTabTapped), for: .touchUpInside)
        return button
    }()
    
    private let loginCardView = LoginCardView()
    private let signUpCardView = SignUpCardView()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setupUI()
        updateTabSelection()
        updateContentView()
    }
    
    private func setupUI() {
        view.backgroundColor = .white
        
        view.addSubview(backgroundGradientView)
        view.addSubview(titleLabel)
        view.addSubview(subtitleLabel)
        view.addSubview(cardView)
        
        cardView.addSubview(tabContainer)
        tabContainer.addSubview(loginTabButton)
        tabContainer.addSubview(signUpTabButton)
        
        cardView.addSubview(loginCardView)
        cardView.addSubview(signUpCardView)
        
        backgroundGradientView.snp.makeConstraints { make in
            make.edges.equalToSuperview()
        }
        
        titleLabel.snp.makeConstraints { make in
            make.top.equalTo(view.safeAreaLayoutGuide).offset(60)
            make.leading.trailing.equalToSuperview().inset(24)
        }
        
        subtitleLabel.snp.makeConstraints { make in
            make.top.equalTo(titleLabel.snp.bottom).offset(8)
            make.leading.trailing.equalToSuperview().inset(24)
        }
        
        cardView.snp.makeConstraints { make in
            make.top.equalTo(subtitleLabel.snp.bottom).offset(40)
            make.leading.trailing.equalToSuperview().inset(24)
            cardHeightConstraint = make.height.equalTo(442).constraint
        }
        
        tabContainer.snp.makeConstraints { make in
            make.top.equalToSuperview().offset(24)
            make.leading.trailing.equalToSuperview().inset(24)
            make.height.equalTo(44)
        }
        
        loginTabButton.snp.makeConstraints { make in
            make.top.bottom.equalToSuperview().inset(4)
            make.leading.equalToSuperview().offset(4)
            make.trailing.equalTo(tabContainer.snp.centerX).offset(-2)
        }
        
        signUpTabButton.snp.makeConstraints { make in
            make.top.bottom.equalToSuperview().inset(4)
            make.leading.equalTo(tabContainer.snp.centerX).offset(2)
            make.trailing.equalToSuperview().offset(-4)
        }
        
        loginCardView.snp.makeConstraints { make in
            make.top.equalTo(tabContainer.snp.bottom)
            make.leading.trailing.bottom.equalToSuperview()
        }
        
        signUpCardView.snp.makeConstraints { make in
            make.top.equalTo(tabContainer.snp.bottom)
            make.leading.trailing.bottom.equalToSuperview()
        }
    }
    
    private func updateTabSelection() {
        if isLoginSelected {
            loginTabButton.backgroundColor = UIColor(hex: "#7E22CE")
            loginTabButton.setTitleColor(.white, for: .normal)
            signUpTabButton.backgroundColor = .clear
            signUpTabButton.setTitleColor(.gray, for: .normal)
        } else {
            loginTabButton.backgroundColor = .clear
            loginTabButton.setTitleColor(.gray, for: .normal)
            signUpTabButton.backgroundColor = UIColor(hex: "#7E22CE")
            signUpTabButton.setTitleColor(.white, for: .normal)
        }
    }
    
    private func updateContentView() {
        loginCardView.isHidden = !isLoginSelected
        signUpCardView.isHidden = isLoginSelected
    }
    
    private func updateCardHeight() {
        UIView.animate(withDuration: 0.3) {
            self.cardHeightConstraint?.update(offset: self.isLoginSelected ? 423 : 442)
            self.view.layoutIfNeeded()
        }
    }
    
    // MARK: - Actions
    @objc private func loginTabTapped() {
        isLoginSelected = true
    }
    
    @objc private func signUpTabTapped() {
        isLoginSelected = false
    }
}
