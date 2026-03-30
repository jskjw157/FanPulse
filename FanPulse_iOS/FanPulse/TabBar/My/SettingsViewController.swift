//
//  SettingsViewController.swift
//  FanPulse
//
//  Created by 김송 on 1/18/26.
//

import UIKit
import SnapKit

final class SettingsViewController: UIViewController {
    
    // MARK: - UI Components
    
    private let scrollView: UIScrollView = {
        let scrollView = UIScrollView()
        scrollView.showsVerticalScrollIndicator = false
        scrollView.backgroundColor = UIColor(hex: "#F9FAFB")
        return scrollView
    }()
    
    private let contentView: UIView = {
        let view = UIView()
        view.backgroundColor = .clear
        return view
    }()
    
    // MARK: - Section Headers
    
    private let accountHeaderLabel = SectionHeaderLabel(text: "계정")
    private let notificationHeaderLabel = SectionHeaderLabel(text: "알림")
    private let appearanceHeaderLabel = SectionHeaderLabel(text: "화면")
    private let supportHeaderLabel = SectionHeaderLabel(text: "지원")
    
    // MARK: - Section Containers
    
    private let accountSectionView: UIView = {
        let view = UIView()
        view.backgroundColor = .white
        view.layer.cornerRadius = 12
        return view
    }()
    
    private let accountStackView: UIStackView = {
        let stackView = UIStackView()
        stackView.axis = .vertical
        stackView.spacing = 0
        return stackView
    }()
    
    private let notificationSectionView: UIView = {
        let view = UIView()
        view.backgroundColor = .white
        view.layer.cornerRadius = 12
        return view
    }()
    
    private let notificationStackView: UIStackView = {
        let stackView = UIStackView()
        stackView.axis = .vertical
        stackView.spacing = 0
        return stackView
    }()
    
    private let appearanceSectionView: UIView = {
        let view = UIView()
        view.backgroundColor = .white
        view.layer.cornerRadius = 12
        return view
    }()
    
    private let appearanceStackView: UIStackView = {
        let stackView = UIStackView()
        stackView.axis = .vertical
        stackView.spacing = 0
        return stackView
    }()
    
    private let supportSectionView: UIView = {
        let view = UIView()
        view.backgroundColor = .white
        view.layer.cornerRadius = 12
        return view
    }()
    
    private let supportStackView: UIStackView = {
        let stackView = UIStackView()
        stackView.axis = .vertical
        stackView.spacing = 0
        return stackView
    }()
    
    // MARK: - Bottom Buttons
    
    private let logoutButton: UIButton = {
        let button = UIButton(type: .system)
        button.setTitle("로그아웃", for: .normal)
        button.titleLabel?.font = .systemFont(ofSize: 15, weight: .medium)
        button.setTitleColor(UIColor(hex: "#6B7280"), for: .normal)
        button.backgroundColor = .white
        button.layer.cornerRadius = 12
        return button
    }()
    
    private let deleteAccountButton: UIButton = {
        let button = UIButton(type: .system)
        button.setTitle("회원 탈퇴", for: .normal)
        button.titleLabel?.font = .systemFont(ofSize: 15, weight: .medium)
        button.setTitleColor(UIColor(hex: "#EF4444"), for: .normal)
        button.backgroundColor = .white
        return button
    }()
    
    // MARK: - Lifecycle
    
    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = UIColor(hex: "#F9FAFB")
        
        setupNavigationBar()
        setupUI()
        setupSections()
    }
    
    // MARK: - Setup
    
    private func setupNavigationBar() {
        title = "설정"
        
        navigationController?.navigationBar.backgroundColor = .white
        navigationController?.navigationBar.shadowImage = UIImage()
        
        let backButton = UIBarButtonItem(
            image: UIImage(systemName: "arrow.left"),
            style: .plain,
            target: self,
            action: #selector(backButtonTapped)
        )
        backButton.tintColor = UIColor(hex: "#111827")
        navigationItem.leftBarButtonItem = backButton
    }
    
    private func setupUI() {
        view.addSubview(scrollView)
        scrollView.addSubview(contentView)
        
        scrollView.snp.makeConstraints { make in
            make.edges.equalTo(view.safeAreaLayoutGuide)
        }
        
        contentView.snp.makeConstraints { make in
            make.edges.equalToSuperview()
            make.width.equalToSuperview()
        }
        
        // Account Section
        contentView.addSubview(accountHeaderLabel)
        contentView.addSubview(accountSectionView)
        accountSectionView.addSubview(accountStackView)
        
        accountHeaderLabel.snp.makeConstraints { make in
            make.top.equalToSuperview().offset(16)
            make.leading.equalToSuperview().offset(24)
        }
        
        accountSectionView.snp.makeConstraints { make in
            make.top.equalTo(accountHeaderLabel.snp.bottom).offset(12)
            make.horizontalEdges.equalToSuperview().inset(16)
        }
        
        accountStackView.snp.makeConstraints { make in
            make.edges.equalToSuperview()
        }
        
        // Notification Section
        contentView.addSubview(notificationHeaderLabel)
        contentView.addSubview(notificationSectionView)
        notificationSectionView.addSubview(notificationStackView)
        
        notificationHeaderLabel.snp.makeConstraints { make in
            make.top.equalTo(accountSectionView.snp.bottom).offset(24)
            make.leading.equalToSuperview().offset(24)
        }
        
        notificationSectionView.snp.makeConstraints { make in
            make.top.equalTo(notificationHeaderLabel.snp.bottom).offset(12)
            make.horizontalEdges.equalToSuperview().inset(16)
        }
        
        notificationStackView.snp.makeConstraints { make in
            make.edges.equalToSuperview()
        }
        
        // Appearance Section
        contentView.addSubview(appearanceHeaderLabel)
        contentView.addSubview(appearanceSectionView)
        appearanceSectionView.addSubview(appearanceStackView)
        
        appearanceHeaderLabel.snp.makeConstraints { make in
            make.top.equalTo(notificationSectionView.snp.bottom).offset(24)
            make.leading.equalToSuperview().offset(24)
        }
        
        appearanceSectionView.snp.makeConstraints { make in
            make.top.equalTo(appearanceHeaderLabel.snp.bottom).offset(12)
            make.horizontalEdges.equalToSuperview().inset(16)
        }
        
        appearanceStackView.snp.makeConstraints { make in
            make.edges.equalToSuperview()
        }
        
        // Support Section
        contentView.addSubview(supportHeaderLabel)
        contentView.addSubview(supportSectionView)
        supportSectionView.addSubview(supportStackView)
        
        supportHeaderLabel.snp.makeConstraints { make in
            make.top.equalTo(appearanceSectionView.snp.bottom).offset(24)
            make.leading.equalToSuperview().offset(24)
        }
        
        supportSectionView.snp.makeConstraints { make in
            make.top.equalTo(supportHeaderLabel.snp.bottom).offset(12)
            make.horizontalEdges.equalToSuperview().inset(16)
        }
        
        supportStackView.snp.makeConstraints { make in
            make.edges.equalToSuperview()
        }
        
        // Bottom Buttons
        contentView.addSubview(logoutButton)
        contentView.addSubview(deleteAccountButton)
        
        logoutButton.snp.makeConstraints { make in
            make.top.equalTo(supportSectionView.snp.bottom).offset(24)
            make.horizontalEdges.equalToSuperview().inset(16)
            make.height.equalTo(52)
        }
        
        deleteAccountButton.snp.makeConstraints { make in
            make.top.equalTo(logoutButton.snp.bottom).offset(16)
            make.centerX.equalToSuperview()
            make.bottom.equalToSuperview().inset(24)
        }
        
        logoutButton.addTarget(self, action: #selector(logoutButtonTapped), for: .touchUpInside)
        deleteAccountButton.addTarget(self, action: #selector(deleteAccountButtonTapped), for: .touchUpInside)
    }
    
    private func setupSections() {
        // Account Section
        let profileItem = SettingsItemView(
            icon: "person.fill",
            title: "프로필 수정",
            iconColor: UIColor(hex: "#A78BFA"),
            iconBackgroundColor: UIColor(hex: "#EDE9FE"),
            hasChevron: true
        )
        profileItem.onTap = { [weak self] in
            self?.handleProfileEdit()
        }
        
        let passwordItem = SettingsItemView(
            icon: "lock.fill",
            title: "비밀번호 변경",
            iconColor: UIColor(hex: "#60A5FA"),
            iconBackgroundColor: UIColor(hex: "#DBEAFE"),
            hasChevron: true
        )
        passwordItem.onTap = { [weak self] in
            self?.handlePasswordChange()
        }
        
        let privacyItem = SettingsItemView(
            icon: "shield.fill",
            title: "개인정보 보호",
            iconColor: UIColor(hex: "#34D399"),
            iconBackgroundColor: UIColor(hex: "#D1FAE5"),
            hasChevron: true
        )
        privacyItem.onTap = { [weak self] in
            self?.handlePrivacySettings()
        }
        
        accountStackView.addArrangedSubview(profileItem)
        accountStackView.addArrangedSubview(createSeparator())
        accountStackView.addArrangedSubview(passwordItem)
        accountStackView.addArrangedSubview(createSeparator())
        accountStackView.addArrangedSubview(privacyItem)
        
        // Notification Section
        let pushToggleItem = SettingsToggleItemView(
            icon: "bell.fill",
            title: "푸시 알림",
            iconColor: UIColor(hex: "#F472B6"),
            iconBackgroundColor: UIColor(hex: "#FCE7F3"),
            isOn: true
        )
        pushToggleItem.onToggleChanged = { [weak self] isOn in
            self?.handlePushNotificationToggle(isOn: isOn)
        }
        
        let dailyNotificationItem = SettingsItemView(
            icon: "gearshape.fill",
            title: "알림 설정",
            iconColor: UIColor(hex: "#FB923C"),
            iconBackgroundColor: UIColor(hex: "#FED7AA"),
            hasChevron: true
        )
        dailyNotificationItem.onTap = { [weak self] in
            self?.handleNotificationSettings()
        }
        
        notificationStackView.addArrangedSubview(pushToggleItem)
        notificationStackView.addArrangedSubview(createSeparator())
        notificationStackView.addArrangedSubview(dailyNotificationItem)
        
        // Appearance Section
        let darkModeItem = SettingsToggleItemView(
            icon: "moon.fill",
            title: "다크 모드",
            iconColor: UIColor(hex: "#818CF8"),
            iconBackgroundColor: UIColor(hex: "#E0E7FF"),
            isOn: false
        )
        darkModeItem.onToggleChanged = { [weak self] isOn in
            self?.handleDarkModeToggle(isOn: isOn)
        }
        
        let languageItem = SettingsItemView(
            icon: "globe",
            title: "언어",
            subtitle: "한국어",
            iconColor: UIColor(hex: "#2DD4BF"),
            iconBackgroundColor: UIColor(hex: "#CCFBF1"),
            hasChevron: true
        )
        languageItem.onTap = { [weak self] in
            self?.handleLanguageSettings()
        }
        
        appearanceStackView.addArrangedSubview(darkModeItem)
        appearanceStackView.addArrangedSubview(createSeparator())
        appearanceStackView.addArrangedSubview(languageItem)
        
        // Support Section
        let helpItem = SettingsItemView(
            icon: "questionmark.circle.fill",
            title: "도움말",
            iconColor: UIColor(hex: "#FDE047"),
            iconBackgroundColor: UIColor(hex: "#FEF9C3"),
            hasChevron: true
        )
        helpItem.onTap = { [weak self] in
            self?.handleHelp()
        }
        
        let customerServiceItem = SettingsItemView(
            icon: "headphones",
            title: "고객센터",
            iconColor: UIColor(hex: "#FB7185"),
            iconBackgroundColor: UIColor(hex: "#FFE4E6"),
            hasChevron: true
        )
        customerServiceItem.onTap = { [weak self] in
            self?.handleCustomerService()
        }
        
        let appInfoItem = SettingsItemView(
            icon: "info.circle",
            title: "앱 정보",
            subtitle: "버전 1.0.0",
            iconColor: UIColor(hex: "#9CA3AF"),
            iconBackgroundColor: UIColor(hex: "#F3F4F6"),
            hasChevron: true
        )
        appInfoItem.onTap = { [weak self] in
            self?.handleAppInfo()
        }
        
        supportStackView.addArrangedSubview(helpItem)
        supportStackView.addArrangedSubview(createSeparator())
        supportStackView.addArrangedSubview(customerServiceItem)
        supportStackView.addArrangedSubview(createSeparator())
        supportStackView.addArrangedSubview(appInfoItem)
    }
    
    // MARK: - Action Handlers
    
    private func handleProfileEdit() {
        print("✏️ 프로필 수정 클릭")
        // TODO: 프로필 수정 화면으로 이동
    }
    
    private func handlePasswordChange() {
        print("🔒 비밀번호 변경 클릭")
        // TODO: 비밀번호 변경 화면으로 이동
    }
    
    private func handlePrivacySettings() {
        print("🛡️ 개인정보 보호 클릭")
        // TODO: 개인정보 보호 설정 화면으로 이동
    }
    
    private func handlePushNotificationToggle(isOn: Bool) {
        print("🔔 푸시 알림 토글: \(isOn ? "켜짐" : "꺼짐")")
        // TODO: 푸시 알림 설정 저장
    }
    
    private func handleNotificationSettings() {
        print("⚙️ 알림 설정 클릭")
        // TODO: 알림 상세 설정 화면으로 이동
    }
    
    private func handleDarkModeToggle(isOn: Bool) {
        print("🌙 다크 모드 토글: \(isOn ? "켜짐" : "꺼짐")")
        // TODO: 다크 모드 설정 적용
    }
    
    private func handleLanguageSettings() {
        print("🌐 언어 설정 클릭")
        // TODO: 언어 선택 화면으로 이동
    }
    
    private func handleHelp() {
        print("❓ 도움말 클릭")
        // TODO: 도움말 화면으로 이동
    }
    
    private func handleCustomerService() {
        print("🎧 고객센터 클릭")
        // TODO: 고객센터 화면으로 이동
    }
    
    private func handleAppInfo() {
        print("ℹ️ 앱 정보 클릭")
        // TODO: 앱 정보 화면으로 이동
    }
    
    private func createSeparator() -> UIView {
        let separator = UIView()
        separator.backgroundColor = UIColor(hex: "#F3F4F6")
        separator.snp.makeConstraints { make in
            make.height.equalTo(1)
        }
        return separator
    }
    
    // MARK: - Actions
    
    @objc private func backButtonTapped() {
        navigationController?.popViewController(animated: true)
    }
    
    @objc private func logoutButtonTapped() {
        let alert = UIAlertController(
            title: "로그아웃",
            message: "정말 로그아웃 하시겠습니까?",
            preferredStyle: .alert
        )
        
        alert.addAction(UIAlertAction(title: "취소", style: .cancel))
        alert.addAction(UIAlertAction(title: "로그아웃", style: .destructive) { [weak self] _ in
            self?.performLogout()
        })
        
        present(alert, animated: true)
    }
    
    @objc private func deleteAccountButtonTapped() {
        let alert = UIAlertController(
            title: "회원 탈퇴",
            message: "정말 탈퇴하시겠습니까? 모든 데이터가 삭제됩니다.",
            preferredStyle: .alert
        )
        
        alert.addAction(UIAlertAction(title: "취소", style: .cancel))
        alert.addAction(UIAlertAction(title: "탈퇴", style: .destructive) { [weak self] _ in
            self?.performDeleteAccount()
        })
        
        present(alert, animated: true)
    }
    
    private func performLogout() {
        KeychainManager.shared.deleteAllTokens()
        let loginVC = LoginViewController()
        let nav = UINavigationController(rootViewController: loginVC)
        nav.modalPresentationStyle = .fullScreen
        
        if let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
           let window = windowScene.windows.first {
            window.rootViewController = nav
            window.makeKeyAndVisible()
        }
    }
    
    private func performDeleteAccount() {
        print("회원 탈퇴 처리")
        // TODO: 회원 탈퇴 API 호출
    }
}

// MARK: - Section Header Label

class SectionHeaderLabel: UILabel {
    init(text: String) {
        super.init(frame: .zero)
        self.text = text
        self.font = .systemFont(ofSize: 13, weight: .semibold)
        self.textColor = UIColor(hex: "#6B7280")
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}

// MARK: - Settings Item View

class SettingsItemView: UIView {
    var onTap: (() -> Void)?
    
    private let iconBackgroundView: UIView = {
        let view = UIView()
        view.layer.cornerRadius = 22
        return view
    }()
    
    private let iconImageView: UIImageView = {
        let imageView = UIImageView()
        imageView.contentMode = .scaleAspectFit
        return imageView
    }()
    
    private let titleLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 15, weight: .medium)
        label.textColor = UIColor(hex: "#111827")
        return label
    }()
    
    private let subtitleLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 13, weight: .regular)
        label.textColor = UIColor(hex: "#9CA3AF")
        return label
    }()
    
    private let chevronImageView: UIImageView = {
        let imageView = UIImageView()
        imageView.image = UIImage(systemName: "chevron.right")
        imageView.tintColor = UIColor(hex: "#D1D5DB")
        imageView.contentMode = .scaleAspectFit
        return imageView
    }()
    
    init(icon: String, title: String, subtitle: String? = nil, iconColor: UIColor, iconBackgroundColor: UIColor, hasChevron: Bool) {
        super.init(frame: .zero)
        
        iconImageView.image = UIImage(systemName: icon)
        iconImageView.tintColor = iconColor
        iconBackgroundView.backgroundColor = iconBackgroundColor
        titleLabel.text = title
        
        if let subtitle = subtitle {
            subtitleLabel.text = subtitle
        }
        
        setupUI(hasChevron: hasChevron, hasSubtitle: subtitle != nil)
        setupGesture()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func setupUI(hasChevron: Bool, hasSubtitle: Bool) {
        addSubview(iconBackgroundView)
        iconBackgroundView.addSubview(iconImageView)
        addSubview(titleLabel)
        
        if hasSubtitle {
            addSubview(subtitleLabel)
        }
        
        if hasChevron {
            addSubview(chevronImageView)
        }
        
        iconBackgroundView.snp.makeConstraints { make in
            make.leading.equalToSuperview().offset(16)
            make.centerY.equalToSuperview()
            make.width.height.equalTo(44)
        }
        
        iconImageView.snp.makeConstraints { make in
            make.center.equalToSuperview()
            make.width.height.equalTo(22)
        }
        
        titleLabel.snp.makeConstraints { make in
            make.leading.equalTo(iconBackgroundView.snp.trailing).offset(16)
            make.centerY.equalToSuperview()
        }
        
        if hasSubtitle {
            subtitleLabel.snp.makeConstraints { make in
                make.trailing.equalToSuperview().inset(hasChevron ? 44 : 16)
                make.centerY.equalToSuperview()
            }
        }
        
        if hasChevron {
            chevronImageView.snp.makeConstraints { make in
                make.trailing.equalToSuperview().inset(16)
                make.centerY.equalToSuperview()
                make.width.equalTo(8)
                make.height.equalTo(14)
            }
        }
        
        snp.makeConstraints { make in
            make.height.equalTo(64)
        }
    }
    
    private func setupGesture() {
        let tapGesture = UITapGestureRecognizer(target: self, action: #selector(handleTap))
        addGestureRecognizer(tapGesture)
        isUserInteractionEnabled = true
    }
    
    @objc private func handleTap() {
        // 탭 애니메이션
        UIView.animate(withDuration: 0.1, animations: {
            self.alpha = 0.5
        }) { _ in
            UIView.animate(withDuration: 0.1) {
                self.alpha = 1.0
            }
        }
        
        onTap?()
    }
}

// MARK: - Settings Toggle Item View

class SettingsToggleItemView: UIView {
    var onToggleChanged: ((Bool) -> Void)?
    
    private let iconBackgroundView: UIView = {
        let view = UIView()
        view.layer.cornerRadius = 22
        return view
    }()
    
    private let iconImageView: UIImageView = {
        let imageView = UIImageView()
        imageView.contentMode = .scaleAspectFit
        return imageView
    }()
    
    private let titleLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 15, weight: .medium)
        label.textColor = UIColor(hex: "#111827")
        return label
    }()
    
    private let toggleSwitch: UISwitch = {
        let toggle = UISwitch()
        toggle.onTintColor = UIColor(hex: "#8B5CF6")
        return toggle
    }()
    
    init(icon: String, title: String, iconColor: UIColor, iconBackgroundColor: UIColor, isOn: Bool) {
        super.init(frame: .zero)
        
        iconImageView.image = UIImage(systemName: icon)
        iconImageView.tintColor = iconColor
        iconBackgroundView.backgroundColor = iconBackgroundColor
        titleLabel.text = title
        toggleSwitch.isOn = isOn
        
        setupUI()
        setupAction()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func setupUI() {
        addSubview(iconBackgroundView)
        iconBackgroundView.addSubview(iconImageView)
        addSubview(titleLabel)
        addSubview(toggleSwitch)
        
        iconBackgroundView.snp.makeConstraints { make in
            make.leading.equalToSuperview().offset(16)
            make.centerY.equalToSuperview()
            make.width.height.equalTo(44)
        }
        
        iconImageView.snp.makeConstraints { make in
            make.center.equalToSuperview()
            make.width.height.equalTo(22)
        }
        
        titleLabel.snp.makeConstraints { make in
            make.leading.equalTo(iconBackgroundView.snp.trailing).offset(16)
            make.centerY.equalToSuperview()
        }
        
        toggleSwitch.snp.makeConstraints { make in
            make.trailing.equalToSuperview().inset(16)
            make.centerY.equalToSuperview()
        }
        
        snp.makeConstraints { make in
            make.height.equalTo(64)
        }
    }
    
    private func setupAction() {
        toggleSwitch.addTarget(self, action: #selector(toggleChanged), for: .valueChanged)
    }
    
    @objc private func toggleChanged() {
        onToggleChanged?(toggleSwitch.isOn)
    }
}
