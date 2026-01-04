//
//  SignUpCardView.swift
//  FanPulse
//
//  Created by 김송 on 12/28/25.
//

import UIKit
import SnapKit

class SignUpCardView: UIView {
    
    private lazy var googleSignUpButton: UIButton = {
        let button = UIButton()
        button.backgroundColor = .white
        button.layer.cornerRadius = 25
        button.layer.borderWidth = 1
        button.layer.borderColor = UIColor.gray.withAlphaComponent(0.3).cgColor
        
        let label = UILabel()
        label.text = "Google로 가입하기"
        label.font = .systemFont(ofSize: 15, weight: .medium)
        label.textColor = .black
        button.addSubview(label)
        label.snp.makeConstraints { make in
            make.centerX.equalToSuperview().offset(16)
            make.centerY.equalToSuperview()
        }
        
        let imageView = UIImageView()
        imageView.image = UIImage(named: "googleIcon")
        imageView.contentMode = .scaleAspectFit
        button.addSubview(imageView)
        imageView.snp.makeConstraints { make in
            make.trailing.equalTo(label.snp.leading).offset(-12)
            make.centerY.equalToSuperview()
            make.width.height.equalTo(20)
        }
        
        button.addTarget(self, action: #selector(googleSignUpTapped), for: .touchUpInside)
        return button
    }()
    
    private let dividerContainer = UIView()
    private let leftLine: UIView = {
        let view = UIView()
        view.backgroundColor = UIColor.gray.withAlphaComponent(0.3)
        return view
    }()
    private let orLabel: UILabel = {
        let label = UILabel()
        label.text = "또는"
        label.font = .systemFont(ofSize: 13)
        label.textColor = .gray
        return label
    }()
    private let rightLine: UIView = {
        let view = UIView()
        view.backgroundColor = UIColor.gray.withAlphaComponent(0.3)
        return view
    }()
    
    private let emailTextField: UITextField = {
        let tf = UITextField()
        tf.autocapitalizationType = .none
        tf.keyboardType = .emailAddress
        tf.backgroundColor = UIColor.gray.withAlphaComponent(0.1)
        tf.layer.cornerRadius = 22
        tf.leftView = UIView(frame: CGRect(x: 0, y: 0, width: 16, height: 0))
        tf.leftViewMode = .always
        tf.placeholder = "이메일"
        return tf
    }()
    
    private let passwordTextField: UITextField = {
        let tf = UITextField()
        tf.isSecureTextEntry = true
        tf.backgroundColor = UIColor.gray.withAlphaComponent(0.1)
        tf.layer.cornerRadius = 22
        tf.leftView = UIView(frame: CGRect(x: 0, y: 0, width: 16, height: 0))
        tf.leftViewMode = .always
        tf.placeholder = "비밀번호"
        return tf
    }()
    
    private let confirmPasswordTextField: UITextField = {
        let tf = UITextField()
        tf.isSecureTextEntry = true
        tf.backgroundColor = UIColor.gray.withAlphaComponent(0.1)
        tf.layer.cornerRadius = 22
        tf.leftView = UIView(frame: CGRect(x: 0, y: 0, width: 16, height: 0))
        tf.leftViewMode = .always
        tf.placeholder = "비밀번호 확인"
        return tf
    }()
    
    private lazy var signUpButton: UIButton = {
        let button = UIButton()
        button.setTitle("회원가입", for: .normal)
        button.titleLabel?.font = .systemFont(ofSize: 16, weight: .semibold)
        button.setTitleColor(.white, for: .normal)
        button.layer.cornerRadius = 22
        button.clipsToBounds = true
        button.addTarget(self, action: #selector(signUpActionTapped), for: .touchUpInside)
        return button
    }()
    
    private let signUpButtonGradientLayer = CAGradientLayer()
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        setupUI()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        setupGradient()
    }
    
    // MARK: - Setup
    private func setupUI() {
        addSubview(googleSignUpButton)
        addSubview(dividerContainer)
        dividerContainer.addSubview(leftLine)
        dividerContainer.addSubview(orLabel)
        dividerContainer.addSubview(rightLine)
        addSubview(emailTextField)
        addSubview(passwordTextField)
        addSubview(confirmPasswordTextField)
        addSubview(signUpButton)
        
        googleSignUpButton.snp.makeConstraints { make in
            make.top.equalToSuperview().offset(24)
            make.leading.trailing.equalToSuperview().inset(24)
            make.height.equalTo(49)
        }
        
        dividerContainer.snp.makeConstraints { make in
            make.top.equalTo(googleSignUpButton.snp.bottom).offset(24)
            make.leading.trailing.equalToSuperview().inset(16)
            make.height.equalTo(20)
        }
        
        leftLine.snp.makeConstraints { make in
            make.leading.equalToSuperview()
            make.centerY.equalToSuperview()
            make.trailing.equalTo(orLabel.snp.leading).offset(-12)
            make.height.equalTo(1)
        }
        
        orLabel.snp.makeConstraints { make in
            make.center.equalToSuperview()
        }
        
        rightLine.snp.makeConstraints { make in
            make.leading.equalTo(orLabel.snp.trailing).offset(12)
            make.centerY.equalToSuperview()
            make.trailing.equalToSuperview()
            make.height.equalTo(1)
        }
        
        emailTextField.snp.makeConstraints { make in
            make.top.equalTo(dividerContainer.snp.bottom).offset(24)
            make.leading.trailing.equalToSuperview().inset(24)
            make.height.equalTo(44)
        }
        
        passwordTextField.snp.makeConstraints { make in
            make.top.equalTo(emailTextField.snp.bottom).offset(12)
            make.leading.trailing.equalToSuperview().inset(24)
            make.height.equalTo(44)
        }
        
        confirmPasswordTextField.snp.makeConstraints { make in
            make.top.equalTo(passwordTextField.snp.bottom).offset(12)
            make.leading.trailing.equalToSuperview().inset(24)
            make.height.equalTo(44)
        }
        
        signUpButton.snp.makeConstraints { make in
            make.top.equalTo(confirmPasswordTextField.snp.bottom).offset(16)
            make.leading.trailing.equalToSuperview().inset(24)
            make.height.equalTo(45)
        }
    }
    
    private func setupGradient() {
        signUpButtonGradientLayer.frame = signUpButton.bounds
        signUpButtonGradientLayer.colors = [
            UIColor(hex: "#EC4899").cgColor,
            UIColor(hex: "#9333EA").cgColor
        ]
        signUpButtonGradientLayer.startPoint = CGPoint(x: 0, y: 0.5)
        signUpButtonGradientLayer.endPoint = CGPoint(x: 1, y: 0.5)
        signUpButtonGradientLayer.cornerRadius = 22.5
        
        if signUpButton.layer.sublayers?.first(where: { $0 is CAGradientLayer }) == nil {
            signUpButton.layer.insertSublayer(signUpButtonGradientLayer, at: 0)
        }
    }
}

extension SignUpCardView {
    @objc private func googleSignUpTapped() {
        print("Google 회원가입")
    }
    
    @objc private func signUpActionTapped() {
        print("회원가입: \(emailTextField.text ?? "")")
    }
}
