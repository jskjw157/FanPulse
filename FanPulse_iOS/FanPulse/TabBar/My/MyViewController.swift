//
//  MyViewController.swift
//  FanPulse
//
//  Created by 김송 on 1/10/26.
//

import UIKit
import SnapKit

final class MyViewController: BaseViewController {

    // MARK: - UI Components
    
    private let scrollView: UIScrollView = {
        let scrollView = UIScrollView()
        scrollView.showsVerticalScrollIndicator = false
        scrollView.backgroundColor = UIColor(hex: "#F3F4F6")
        return scrollView
    }()
    
    private let contentView: UIView = {
        let view = UIView()
        view.backgroundColor = .clear
        return view
    }()
    
    // MARK: - Profile Section
    
    private let profileBackgroundImageView: UIImageView = {
        let imageView = UIImageView()
        imageView.contentMode = .scaleAspectFill
        imageView.clipsToBounds = true
        imageView.image = UIImage(named: "img_myBg")
        return imageView
    }()
    
    private let gradientView: UIView = {
        let view = UIView()
        return view
    }()
    
    private let profileImageView: UIImageView = {
        let imageView = UIImageView()
        imageView.contentMode = .scaleAspectFill
        imageView.clipsToBounds = true
        imageView.layer.cornerRadius = 40
        imageView.layer.borderWidth = 3
        imageView.layer.borderColor = UIColor.white.cgColor
        imageView.image = UIImage(named: "img_profile")
        return imageView
    }()
    
    private let nameLabel: UILabel = {
        let label = UILabel()
        label.text = "Sarah Kim"
        label.font = .systemFont(ofSize: 20, weight: .bold)
        label.textColor = .white
        return label
    }()
    
    private let vipBadge: UIView = {
        let view = UIView()
        view.backgroundColor = UIColor(hex: "#FFD700")
        view.layer.cornerRadius = 10
        return view
    }()
    
    private let vipLabel: UILabel = {
        let label = UILabel()
        label.text = "VIP"
        label.font = .systemFont(ofSize: 11, weight: .bold)
        label.textColor = .white
        return label
    }()
    
    private let emailLabel: UILabel = {
        let label = UILabel()
        label.text = "sarah.kim@example.com"
        label.font = .systemFont(ofSize: 13, weight: .regular)
        label.textColor = UIColor.white.withAlphaComponent(0.9)
        return label
    }()
    
    private let levelLabel: UILabel = {
        let label = UILabel()
        label.text = "Lv.15"
        label.font = .systemFont(ofSize: 12, weight: .semibold)
        label.textColor = .white
        return label
    }()
    
    private let statsStackView: UIStackView = {
        let stackView = UIStackView()
        stackView.axis = .horizontal
        stackView.distribution = .fillEqually
        stackView.spacing = 8
        return stackView
    }()
    
    // MARK: - Point Section
    
    private let pointBackgroundView: UIView = {
        let view = UIView()
        view.backgroundColor = .white
        view.layer.cornerRadius = 16
        view.layer.shadowColor = UIColor.black.cgColor
        view.layer.shadowOpacity = 0.05
        view.layer.shadowOffset = CGSize(width: 0, height: 2)
        view.layer.shadowRadius = 8
        return view
    }()
    
    private let pointTitleLabel: UILabel = {
        let label = UILabel()
        label.text = "보유 포인트"
        label.font = .systemFont(ofSize: 13, weight: .regular)
        label.textColor = UIColor(hex: "#6B7280")
        return label
    }()
    
    private let pointAmountLabel: UILabel = {
        let label = UILabel()
        label.text = "12,450P"
        label.font = .systemFont(ofSize: 28, weight: .bold)
        label.textColor = UIColor(hex: "#9333EA")
        return label
    }()
    
    private let chargeButton: UIButton = {
        let button = UIButton(type: .system)
        button.setTitle("포인트 적립", for: .normal)
        button.titleLabel?.font = .systemFont(ofSize: 14, weight: .semibold)
        button.setTitleColor(.white, for: .normal)
        button.backgroundColor = UIColor(hex: "#9333EA")
        button.layer.cornerRadius = 20
        return button
    }()
    
    private let pointHistoryTitleLabel: UILabel = {
        let label = UILabel()
        label.text = "최근 포인트 내역"
        label.font = .systemFont(ofSize: 14, weight: .semibold)
        label.textColor = UIColor(hex: "#111827")
        return label
    }()
    
    private let pointHistoryStackView: UIStackView = {
        let stackView = UIStackView()
        stackView.axis = .vertical
        stackView.spacing = 12
        return stackView
    }()
    
    private let viewAllButton: UIButton = {
        let button = UIButton(type: .system)
        button.setTitle("전체 내역 보기 〉", for: .normal)
        button.titleLabel?.font = .systemFont(ofSize: 14, weight: .medium)
        button.setTitleColor(UIColor(hex: "#9333EA"), for: .normal)
        return button
    }()
    
    // MARK: - Menu List Section
    
    private let myListBackgroundView: UIView = {
        let view = UIView()
        view.backgroundColor = .white
        view.layer.cornerRadius = 16
        view.layer.shadowColor = UIColor.black.cgColor
        view.layer.shadowOpacity = 0.05
        view.layer.shadowOffset = CGSize(width: 0, height: 2)
        view.layer.shadowRadius = 8
        return view
    }()
    
    private let menuStackView: UIStackView = {
        let stackView = UIStackView()
        stackView.axis = .vertical
        stackView.spacing = 0
        return stackView
    }()
    
    // MARK: - Logout Button
    
    private let logoutButtonView: UIView = {
        let view = UIView()
        view.backgroundColor = .white
        view.layer.cornerRadius = 16
        view.layer.shadowColor = UIColor.black.cgColor
        view.layer.shadowOpacity = 0.05
        view.layer.shadowOffset = CGSize(width: 0, height: 2)
        view.layer.shadowRadius = 8
        return view
    }()
    
    private let logoutButtonLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 14, weight: .medium)
        label.textAlignment = .center
        label.text = "로그아웃"
        label.textColor = UIColor(hex: "#6B7280")
        label.isUserInteractionEnabled = false
        return label
    }()
    
    // MARK: - Lifecycle

    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = UIColor(hex: "#F3F4F6")
        
        configureNavigationBar(type: .my)
        setNavigationTitle("My")
        
        onNotificationTapped = {
            let vc = NotificationViewController()
            self.navigationController?.pushViewController(vc, animated: true)
        }
        
        onSettingTapped = {
            let vc = SettingsViewController()
            self.navigationController?.pushViewController(vc, animated: true)
        }
        
        let tapGesture = UITapGestureRecognizer(target: self, action: #selector(logoutButtonTapped))
        logoutButtonView.addGestureRecognizer(tapGesture)
        logoutButtonView.isUserInteractionEnabled = true
        
        setupUI()
        setupGradient()
        setupStatsCards()
        setupPointHistory()
        setupMenuItems()
    }
    
    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        updateGradient()
    }
    
    // MARK: - Setup
    
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
        
        setupProfileSection()
        setupPointSection()
        setupMenuSection()
        setupLogoutButton()
    }
    
    private func setupProfileSection() {
        contentView.addSubview(profileBackgroundImageView)
        profileBackgroundImageView.addSubview(gradientView)
        profileBackgroundImageView.addSubview(profileImageView)
        profileBackgroundImageView.addSubview(nameLabel)
        profileBackgroundImageView.addSubview(vipBadge)
        vipBadge.addSubview(vipLabel)
        profileBackgroundImageView.addSubview(emailLabel)
        profileBackgroundImageView.addSubview(levelLabel)
        profileBackgroundImageView.addSubview(statsStackView)
        
        profileBackgroundImageView.snp.makeConstraints { make in
            make.leading.trailing.top.equalToSuperview()
            make.height.equalTo(244)
        }
        
        gradientView.snp.makeConstraints { make in
            make.edges.equalToSuperview()
        }
        
        profileImageView.snp.makeConstraints { make in
            make.top.equalToSuperview().offset(24)
            make.leading.equalToSuperview().offset(24)
            make.width.height.equalTo(80)
        }
        
        nameLabel.snp.makeConstraints { make in
            make.top.equalToSuperview().offset(40)
            make.leading.equalTo(profileImageView.snp.trailing).offset(16)
        }
        
        vipBadge.snp.makeConstraints { make in
            make.centerY.equalTo(nameLabel)
            make.leading.equalTo(nameLabel.snp.trailing).offset(8)
            make.width.equalTo(36)
            make.height.equalTo(20)
        }
        
        vipLabel.snp.makeConstraints { make in
            make.center.equalToSuperview()
        }
        
        emailLabel.snp.makeConstraints { make in
            make.top.equalTo(nameLabel.snp.bottom).offset(4)
            make.leading.equalTo(nameLabel)
        }
        
        levelLabel.snp.makeConstraints { make in
            make.bottom.equalTo(profileImageView)
            make.leading.equalTo(nameLabel)
        }
        
        statsStackView.snp.makeConstraints { make in
            make.leading.trailing.equalToSuperview().inset(24)
            make.bottom.equalToSuperview().inset(20)
            make.height.equalTo(100)
        }
    }
    
    private func setupStatsCards() {
        let stats = [
            ("trophy", "247", "투표 참여"),
            ("doc", "89", "게시글"),
            ("users", "1.2K", "팔로워")
        ]
        
        for stat in stats {
            let card = createStatsCard(icon: stat.0, value: stat.1, label: stat.2)
            statsStackView.addArrangedSubview(card)
        }
    }
    
    private func createStatsCard(icon: String, value: String, label: String) -> UIView {
        let card = UIView()
        card.backgroundColor = UIColor.white.withAlphaComponent(0.2)
        card.layer.cornerRadius = 12
        
        let iconImageView = UIImageView()
        iconImageView.image = UIImage(systemName: icon == "trophy" ? "trophy.fill" : icon == "doc" ? "doc.text.fill" : "person.2.fill")
        iconImageView.tintColor = .white
        iconImageView.contentMode = .scaleAspectFit
        
        let valueLabel = UILabel()
        valueLabel.text = value
        valueLabel.font = .systemFont(ofSize: 18, weight: .bold)
        valueLabel.textColor = .white
        valueLabel.textAlignment = .center
        
        let titleLabel = UILabel()
        titleLabel.text = label
        titleLabel.font = .systemFont(ofSize: 12, weight: .regular)
        titleLabel.textColor = UIColor.white.withAlphaComponent(0.9)
        titleLabel.textAlignment = .center
        
        card.addSubview(iconImageView)
        card.addSubview(valueLabel)
        card.addSubview(titleLabel)
        
        iconImageView.snp.makeConstraints { make in
            make.top.equalToSuperview().offset(16)
            make.centerX.equalToSuperview()
            make.width.height.equalTo(24)
        }
        
        valueLabel.snp.makeConstraints { make in
            make.top.equalTo(iconImageView.snp.bottom)
            make.height.equalTo(28)
            make.centerX.equalToSuperview()
        }
        
        titleLabel.snp.makeConstraints { make in
            make.top.equalTo(valueLabel.snp.bottom)
            make.height.equalTo(16)
            make.centerX.equalToSuperview()
        }
        
        return card
    }
    
    private func setupPointSection() {
        contentView.addSubview(pointBackgroundView)
        pointBackgroundView.addSubview(pointTitleLabel)
        pointBackgroundView.addSubview(pointAmountLabel)
        pointBackgroundView.addSubview(chargeButton)
        pointBackgroundView.addSubview(pointHistoryTitleLabel)
        pointBackgroundView.addSubview(pointHistoryStackView)
        pointBackgroundView.addSubview(viewAllButton)
        
        pointBackgroundView.snp.makeConstraints { make in
            make.top.equalTo(profileBackgroundImageView.snp.bottom).offset(16)
            make.horizontalEdges.equalToSuperview().inset(16)
        }
        
        pointTitleLabel.snp.makeConstraints { make in
            make.top.equalToSuperview().offset(20)
            make.leading.equalToSuperview().offset(20)
        }
        
        pointAmountLabel.snp.makeConstraints { make in
            make.top.equalTo(pointTitleLabel.snp.bottom).offset(4)
            make.leading.equalTo(pointTitleLabel)
        }
        
        chargeButton.snp.makeConstraints { make in
            make.centerY.equalTo(pointAmountLabel)
            make.trailing.equalToSuperview().inset(20)
            make.width.equalTo(100)
            make.height.equalTo(40)
        }
        
        pointHistoryTitleLabel.snp.makeConstraints { make in
            make.top.equalTo(pointAmountLabel.snp.bottom).offset(24)
            make.leading.equalToSuperview().offset(20)
        }
        
        pointHistoryStackView.snp.makeConstraints { make in
            make.top.equalTo(pointHistoryTitleLabel.snp.bottom).offset(16)
            make.leading.trailing.equalToSuperview().inset(20)
        }
        
        viewAllButton.snp.makeConstraints { make in
            make.top.equalTo(pointHistoryStackView.snp.bottom).offset(12)
            make.centerX.equalToSuperview()
            make.bottom.equalToSuperview().inset(17)
        }
    }
    
    private func setupPointHistory() {
        let history = [
            ("광고 시청", "+500", true),
            ("굿즈 구매", "-2000", false),
            ("투표 참여", "+100", true)
        ]
        
        for item in history {
            let row = createPointHistoryRow(title: item.0, amount: item.1, isPositive: item.2)
            pointHistoryStackView.addArrangedSubview(row)
        }
        
        pointHistoryStackView.spacing = 8
    }
    
    private func createPointHistoryRow(title: String, amount: String, isPositive: Bool) -> UIView {
        let container = UIView()
        
        let dot = UIView()
        dot.backgroundColor = isPositive ? UIColor(hex: "#10B981") : UIColor(hex: "#EF4444")
        dot.layer.cornerRadius = 4
        
        let titleLabel = UILabel()
        titleLabel.text = title
        titleLabel.font = .systemFont(ofSize: 14, weight: .medium)
        titleLabel.textColor = UIColor(hex: "#111827")
        
        let amountLabel = UILabel()
        amountLabel.text = amount
        amountLabel.font = .systemFont(ofSize: 14, weight: .medium)
        amountLabel.textColor = isPositive ? UIColor(hex: "#10B981") : UIColor(hex: "#EF4444")
        
        container.addSubview(dot)
        container.addSubview(titleLabel)
        container.addSubview(amountLabel)
        
        dot.snp.makeConstraints { make in
            make.leading.equalToSuperview()
            make.centerY.equalToSuperview()
            make.width.height.equalTo(8)
        }
        
        titleLabel.snp.makeConstraints { make in
            make.leading.equalTo(dot.snp.trailing).offset(16)
            make.centerY.equalToSuperview()
        }
        
        amountLabel.snp.makeConstraints { make in
            make.trailing.equalToSuperview().inset(16)
            make.centerY.equalToSuperview()
        }
        
        container.snp.makeConstraints { make in
            make.height.equalTo(20)
        }
        
        return container
    }
    
    private func setupMenuSection() {
        contentView.addSubview(myListBackgroundView)
        myListBackgroundView.addSubview(menuStackView)
        
        myListBackgroundView.snp.makeConstraints { make in
            make.top.equalTo(pointBackgroundView.snp.bottom).offset(16)
            make.horizontalEdges.equalToSuperview().inset(16)
        }
        
        menuStackView.snp.makeConstraints { make in
            make.edges.equalToSuperview()
        }
    }
    
    private func setupMenuItems() {
        let menuItems = [
            ("DIV-102", "좋아요한 아티스트"),
            ("DIV-112", "저장한 게시글"),
            ("DIV-122", "예매 내역"),
            ("DIV-132", "설정"),
            ("DIV-142", "고객센터")
        ]
        
        for (index, item) in menuItems.enumerated() {
            let menuRow = createMenuRow(icon: item.0, title: item.1)
            menuStackView.addArrangedSubview(menuRow)
            
            if index < menuItems.count - 1 {
                let separator = UIView()
                menuStackView.addArrangedSubview(separator)
                separator.snp.makeConstraints { make in
                    make.height.equalTo(1)
                }
            }
        }
    }
    
    private func createMenuRow(icon: String, title: String) -> UIView {
        let container = UIView()
        container.backgroundColor = .white
        container.layer.cornerRadius = 12
        
        let iconImageView = UIImageView()
        iconImageView.image = UIImage(named: icon)
        iconImageView.contentMode = .scaleAspectFit
        
        let titleLabel = UILabel()
        titleLabel.text = title
        titleLabel.font = .systemFont(ofSize: 15, weight: .medium)
        titleLabel.textColor = UIColor(hex: "#111827")
        
        let chevronImageView = UIImageView()
        chevronImageView.image = UIImage(systemName: "chevron.right")
        chevronImageView.tintColor = UIColor(hex: "#9CA3AF")
        chevronImageView.contentMode = .scaleAspectFit
        
        container.addSubview(iconImageView)
        container.addSubview(titleLabel)
        container.addSubview(chevronImageView)
        
        iconImageView.snp.makeConstraints { make in
            make.leading.equalToSuperview().offset(16)
            make.centerY.equalToSuperview()
            make.width.height.equalTo(40)
        }
        
        titleLabel.snp.makeConstraints { make in
            make.leading.equalTo(iconImageView.snp.trailing).offset(10)
            make.centerY.equalToSuperview()
        }
        
        chevronImageView.snp.makeConstraints { make in
            make.trailing.equalToSuperview().inset(23)
            make.centerY.equalToSuperview()
            make.width.equalTo(8)
            make.height.equalTo(14)
        }
        
        container.snp.makeConstraints { make in
            make.height.equalTo(70)
        }
        
        let tapGesture = UITapGestureRecognizer(target: self, action: #selector(menuItemTapped(_:)))
        container.addGestureRecognizer(tapGesture)
        container.isUserInteractionEnabled = true
        container.tag = menuStackView.arrangedSubviews.count
        
        return container
    }
    
    private func setupLogoutButton() {
        contentView.addSubview(logoutButtonView)
        logoutButtonView.addSubview(logoutButtonLabel)
        
        logoutButtonView.snp.makeConstraints { make in
            make.top.equalTo(myListBackgroundView.snp.bottom).offset(16)
            make.horizontalEdges.equalToSuperview().inset(16)
            make.height.equalTo(56)
            make.bottom.equalToSuperview().inset(16)
        }
        
        logoutButtonLabel.snp.makeConstraints { make in
            make.center.equalToSuperview()
        }
    }
    
    private func setupGradient() {
        let gradientLayer = CAGradientLayer()
        gradientLayer.colors = [
            UIColor.clear.cgColor,
            UIColor.black.withAlphaComponent(0.3).cgColor
        ]
        gradientLayer.locations = [0.0, 1.0]
        gradientView.layer.addSublayer(gradientLayer)
    }
    
    private func updateGradient() {
        if let gradientLayer = gradientView.layer.sublayers?.first as? CAGradientLayer {
            gradientLayer.frame = gradientView.bounds
        }
    }
    
    // MARK: - Actions
    
    @objc private func menuItemTapped(_ gesture: UITapGestureRecognizer) {
        guard let view = gesture.view else { return }
        
        // 탭 애니메이션
        UIView.animate(withDuration: 0.1, animations: {
            view.alpha = 0.5
        }) { _ in
            UIView.animate(withDuration: 0.1) {
                view.alpha = 1.0
            }
        }
        
        // 메뉴 아이템별 처리
        switch view.tag {
        case 0: // 좋아요한 아티스트
            print("좋아요한 아티스트")
        case 2: // 저장한 게시글
            print("저장한 게시글")
        case 4: // 예매 내역
            print("예매 내역")
        case 6: // 설정
            let vc = SettingsViewController()
            navigationController?.pushViewController(vc, animated: true)
        case 8: // 고객센터
            print("고객센터")
        default:
            break
        }
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
    
    private func performLogout() {
        KeychainManager.shared.deleteAllTokens()
        let loginVC = LoginViewController()
        let nav = UINavigationController(rootViewController: loginVC)
        nav.modalPresentationStyle = .fullScreen
        present(nav, animated: true)
    }
}
