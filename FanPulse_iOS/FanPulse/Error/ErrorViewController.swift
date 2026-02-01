//
//  ErrorViewController.swift
//  FanPulse
//
//  Created by 김송 on 1/10/26.
//

import UIKit
import SnapKit

final class ErrorViewController: UIViewController {
    
    private let backgroundView = FadeToBackgroundGradientView(
        baseColor: .systemBackground,
        fadeColor: UIColor(hex: "#F5F3FF"),
        startPoint: CGPoint(x: 0.5, y: 0),
        endPoint: CGPoint(x: 0.5, y: 1)
    )
    
    private let iconView = GradientCircleIconView(
        startColor: UIColor(hex: "#F472B6"),
        endColor: UIColor(hex: "#A855F7"),
        iconName: "icon_error"
    )
    
    private let titleLabel = UILabel()
    private let messageLabel = UILabel()
    private let descriptionLabel = UILabel()
    
    private let primaryButton = GradientButton(
        startColor: UIColor(hex: "#EC4899"),
        endColor: UIColor(hex: "#9333EA")
    )
    
    private let secondaryButton = UIButton(type: .system)
    
    private let footerLabel = UILabel()
    private let supportButton = UIButton(type: .system)
    
    // MARK: - Properties
    
    private let messageText: String
    private let descriptionText: String
    
    private let secondaryAction: (() -> Void)?
    private let supportAction: (() -> Void)?
    
    // MARK: - Init
    
    init(
        message: String,
        description: String,
        secondaryAction: (() -> Void)? = nil,
        supportAction: (() -> Void)? = nil
    ) {
        self.messageText = message
        self.descriptionText = description
        self.secondaryAction = secondaryAction
        self.supportAction = supportAction
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setupUI()
        setupLayout()
        bind()
    }
}

private extension ErrorViewController {
    
    func setupUI() {
        view.backgroundColor = .clear
        
        titleLabel.text = "Oops!"
        titleLabel.font = .systemFont(ofSize: 30, weight: .bold)
        titleLabel.textAlignment = .center
        titleLabel.textColor = .label
        
        messageLabel.text = messageText
        messageLabel.font = .systemFont(ofSize: 18, weight: .semibold)
        messageLabel.textAlignment = .center
        messageLabel.textColor = .secondaryLabel
        
        descriptionLabel.text = descriptionText
        descriptionLabel.font = .systemFont(ofSize: 14)
        descriptionLabel.textAlignment = .center
        descriptionLabel.textColor = .tertiaryLabel
        descriptionLabel.numberOfLines = 0
        
        primaryButton.setTitle("Go to Home", for: .normal)
        primaryButton.setImage(UIImage(systemName: "house.fill"), for: .normal)
        primaryButton.tintColor = .white
        primaryButton.semanticContentAttribute = .forceLeftToRight
        primaryButton.imageEdgeInsets = UIEdgeInsets(top: 0, left: 0, bottom: 0, right: 8)
        
        secondaryButton.setTitle("Go Back", for: .normal)
        secondaryButton.setImage(UIImage(systemName: "arrow.left"), for: .normal)
        secondaryButton.setTitleColor(.label, for: .normal)
        secondaryButton.tintColor = .label
        secondaryButton.titleLabel?.font = .systemFont(ofSize: 16, weight: .medium)
        secondaryButton.backgroundColor = .white
        secondaryButton.layer.cornerRadius = 24
        secondaryButton.layer.shadowColor = UIColor.black.cgColor
        secondaryButton.layer.shadowOpacity = 0.08
        secondaryButton.layer.shadowOffset = CGSize(width: 0, height: 4)
        secondaryButton.layer.shadowRadius = 8
        secondaryButton.semanticContentAttribute = .forceLeftToRight
        secondaryButton.imageEdgeInsets = UIEdgeInsets(top: 0, left: 0, bottom: 0, right: 8)
        
        footerLabel.text = "Need help? Contact our "
        footerLabel.font = .systemFont(ofSize: 13)
        footerLabel.textAlignment = .center
        footerLabel.textColor = .secondaryLabel
        
        supportButton.setTitle("support team", for: .normal)
        supportButton.titleLabel?.font = .systemFont(ofSize: 13, weight: .medium)
        supportButton.setTitleColor(UIColor(hex: "#9333EA"), for: .normal)
    }
}

private extension ErrorViewController {
    
    func setupLayout() {
        view.addSubview(backgroundView)
        backgroundView.snp.makeConstraints { $0.edges.equalToSuperview() }
        
        [
            iconView,
            titleLabel,
            messageLabel,
            descriptionLabel,
            primaryButton,
            secondaryButton
        ].forEach { view.addSubview($0) }
        
        iconView.snp.makeConstraints {
            $0.top.equalTo(view.safeAreaLayoutGuide).offset(64)
            $0.centerX.equalToSuperview()
            $0.size.equalTo(120)
        }
        
        titleLabel.snp.makeConstraints {
            $0.top.equalTo(iconView.snp.bottom).offset(32)
            $0.centerX.equalToSuperview()
        }
        
        messageLabel.snp.makeConstraints {
            $0.top.equalTo(titleLabel.snp.bottom).offset(12)
            $0.centerX.equalToSuperview()
        }
        
        descriptionLabel.snp.makeConstraints {
            $0.top.equalTo(messageLabel.snp.bottom).offset(12)
            $0.leading.trailing.equalToSuperview().inset(32)
        }
        
        primaryButton.snp.makeConstraints {
            $0.top.equalTo(descriptionLabel.snp.bottom).offset(40)
            $0.leading.trailing.equalToSuperview().inset(32)
            $0.height.equalTo(52)
        }
        
        secondaryButton.snp.makeConstraints {
            $0.top.equalTo(primaryButton.snp.bottom).offset(16)
            $0.leading.trailing.equalToSuperview().inset(32)
            $0.height.equalTo(52)
        }
        
        // Footer 컨테이너
        let footerContainer = UIStackView(arrangedSubviews: [footerLabel, supportButton])
        footerContainer.axis = .horizontal
        footerContainer.spacing = 0
        footerContainer.alignment = .center
        view.addSubview(footerContainer)
        
        footerContainer.snp.makeConstraints {
            $0.bottom.equalTo(view.safeAreaLayoutGuide).offset(-24)
            $0.centerX.equalToSuperview()
        }
    }
}

private extension ErrorViewController {
    
    func bind() {
        primaryButton.addTarget(self, action: #selector(didTapGoToHome), for: .touchUpInside)
        secondaryButton.addTarget(self, action: #selector(didTapSecondary), for: .touchUpInside)
        supportButton.addTarget(self, action: #selector(didTapSupport), for: .touchUpInside)
    }
    
    @objc func didTapGoToHome() {
        // 탭바 컨트롤러로 이동 (홈 탭 선택)
        if let sceneDelegate = UIApplication.shared.connectedScenes.first?.delegate as? SceneDelegate,
           let window = sceneDelegate.window,
           let tabBarController = window.rootViewController as? UITabBarController {
            tabBarController.selectedIndex = 0 // 홈 탭
            dismiss(animated: true)
        } else {
            // 네비게이션 스택을 루트로 되돌림
            navigationController?.popToRootViewController(animated: true)
        }
    }
    
    @objc func didTapSecondary() {
        secondaryAction?()
    }
    
    @objc func didTapSupport() {
        supportAction?()
    }
}
