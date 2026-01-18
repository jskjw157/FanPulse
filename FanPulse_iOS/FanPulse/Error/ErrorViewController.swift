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
    
    // MARK: - Properties
    
    private let titleText: String
    private let messageText: String
    private let descriptionText: String
    private let primaryButtonTitle: String
    private let secondaryButtonTitle: String
    private let footerText: String?
    
    private let primaryAction: (() -> Void)?
    private let secondaryAction: (() -> Void)?
    
    // MARK: - Init
    
    init(
        title: String,
        message: String,
        description: String,
        primaryButtonTitle: String,
        secondaryButtonTitle: String,
        footerText: String? = nil,
        primaryAction: (() -> Void)? = nil,
        secondaryAction: (() -> Void)? = nil
    ) {
        self.titleText = title
        self.messageText = message
        self.descriptionText = description
        self.primaryButtonTitle = primaryButtonTitle
        self.secondaryButtonTitle = secondaryButtonTitle
        self.footerText = footerText
        self.primaryAction = primaryAction
        self.secondaryAction = secondaryAction
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
        
        titleLabel.text = titleText
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
        
        primaryButton.setTitle(primaryButtonTitle, for: .normal)
        
        secondaryButton.setTitle(secondaryButtonTitle, for: .normal)
        secondaryButton.setTitleColor(.label, for: .normal)
        secondaryButton.titleLabel?.font = .systemFont(ofSize: 16, weight: .medium)
        secondaryButton.backgroundColor = .white
        secondaryButton.layer.cornerRadius = 24
        secondaryButton.layer.shadowColor = UIColor.black.cgColor
        secondaryButton.layer.shadowOpacity = 0.08
        secondaryButton.layer.shadowOffset = CGSize(width: 0, height: 4)
        secondaryButton.layer.shadowRadius = 8
        
        footerLabel.text = footerText
        footerLabel.font = .systemFont(ofSize: 13)
        footerLabel.textAlignment = .center
        footerLabel.textColor = .secondaryLabel
        footerLabel.numberOfLines = 0
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
            secondaryButton,
            footerLabel
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
        
        footerLabel.snp.makeConstraints {
            $0.bottom.equalTo(view.safeAreaLayoutGuide).offset(-24)
            $0.leading.trailing.equalToSuperview().inset(32)
        }
    }
}

private extension ErrorViewController {
    
    func bind() {
        primaryButton.addTarget(self, action: #selector(didTapPrimary), for: .touchUpInside)
        secondaryButton.addTarget(self, action: #selector(didTapSecondary), for: .touchUpInside)
    }
    
    @objc func didTapPrimary() {
        primaryAction?()
    }
    
    @objc func didTapSecondary() {
        secondaryAction?()
    }
}
