//
//  BaseViewController.swift
//  FanPulse
//
//  Created by 김송 on 1/12/26.
//

import UIKit
import SnapKit

// MARK: - Navigation Types

enum NavigationBarPageType {
    case def
    case home
    case commu
    case live
    case vote
    case my
    case noti
}

enum NavigationBarButton {
    case search
    case notification
    case menu
    case setting
}

// 페이지별 버튼 구성 (순서 중요)
extension NavigationBarPageType {
    var buttons: [NavigationBarButton] {
        switch self {
        case .home:
            return [.search, .notification, .menu]
        case .commu:
            return [.search, .notification]
        case .live:
            return [.search, .notification, .menu]
        case .vote:
            return []
        case .my:
            return [.notification, .setting]
        case .noti:
            return [ .setting]
        case .def:
            return []
        }
    }
}

// MARK: - BaseViewController

class BaseViewController: UIViewController {

    // MARK: - Properties
    
    private var currentPageType: NavigationBarPageType = .def

    // MARK: - Callbacks

    var onSearchTapped: (() -> Void)?
    var onNotificationTapped: (() -> Void)?
    var onMenuTapped: (() -> Void)?
    var onSettingTapped: (() -> Void)?

    // MARK: - UI

    private let titleLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 20, weight: .semibold)
        label.textColor = .black
        return label
    }()
    
    private let titleImageView: UIImageView = {
        let imageView = UIImageView()
        imageView.contentMode = .scaleAspectFit
        imageView.isHidden = true
        return imageView
    }()

    private lazy var rightButtonStackView: UIStackView = {
        let stack = UIStackView()
        stack.axis = .horizontal
        stack.spacing = 8
        stack.alignment = .center
        return stack
    }()

    private let searchButton = BaseViewController.makeButton(imageName: "icon_magnifyingglass")
    private let notificationButton = BaseViewController.makeButton(imageName: "icon_bell")
    private let menuButton = BaseViewController.makeButton(imageName: "icon_line.3.horizontal")
    private let settingButton = BaseViewController.makeButton(imageName: "icon_set")

    // MARK: - Lifecycle

    override func viewDidLoad() {
        super.viewDidLoad()
        setupNavigationBar()
        setupRightButtons()
        setupActions()
    }

    // MARK: - Setup

    private func setupNavigationBar() {
        navigationController?.navigationBar.prefersLargeTitles = false
        navigationController?.navigationBar.tintColor = .black

        let appearance = UINavigationBarAppearance()
        appearance.configureWithOpaqueBackground()
        appearance.backgroundColor = .white
        appearance.shadowColor = .systemGray5

        navigationController?.navigationBar.standardAppearance = appearance
        navigationController?.navigationBar.scrollEdgeAppearance = appearance

        navigationItem.leftBarButtonItem = UIBarButtonItem(customView: titleLabel)
    }

    private func setupRightButtons() {
        [
            searchButton,
            notificationButton,
            menuButton,
            settingButton
        ].forEach {
            rightButtonStackView.addArrangedSubview($0)
            $0.snp.makeConstraints { make in
                make.width.height.equalTo(32)
            }
        }

        navigationItem.rightBarButtonItem = UIBarButtonItem(customView: rightButtonStackView)
    }

    private func setupActions() {
        searchButton.addTarget(self, action: #selector(searchTapped), for: .touchUpInside)
        notificationButton.addTarget(self, action: #selector(notificationTapped), for: .touchUpInside)
        menuButton.addTarget(self, action: #selector(menuTapped), for: .touchUpInside)
        settingButton.addTarget(self, action: #selector(settingTapped), for: .touchUpInside)
    }

    // MARK: - Actions

    @objc private func searchTapped() {
        onSearchTapped?()
    }

    @objc private func notificationTapped() {
        onNotificationTapped?()
    }

    @objc private func menuTapped() {
        onMenuTapped?()
    }

    @objc private func settingTapped() {
        onSettingTapped?()
    }

    // MARK: - Public

    func setNavigationTitle(_ title: String? = nil) {
        if let title = title {
            titleLabel.isHidden = false
            titleImageView.isHidden = true
            titleLabel.text = title
            navigationItem.leftBarButtonItem = UIBarButtonItem(customView: titleLabel)
        } else {
            titleLabel.isHidden = true
            titleImageView.isHidden = false
            titleImageView.image = UIImage(named: "logo")
            navigationItem.leftBarButtonItem = UIBarButtonItem(customView: titleImageView)
            titleImageView.snp.makeConstraints { make in
                make.height.equalTo(28)
                make.width.equalTo(81)
            }
        }
    }
    
    func setNavigationBackButton() {
        let backItem = UIBarButtonItem(
            image: UIImage(named: "icon_chev"),
            style: .plain,
            target: self,
            action: #selector(didTapBack)
        )
        navigationItem.leftBarButtonItem = backItem

        navigationController?.interactivePopGestureRecognizer?.isEnabled = true
    }
    
    @objc private func didTapBack() {
        navigationController?.popViewController(animated: true)
    }

    func configureNavigationBar(type: NavigationBarPageType, setBgImage: Bool = false) {
        currentPageType = type
        let visibleButtons = type.buttons

        searchButton.isHidden = !visibleButtons.contains(.search)
        notificationButton.isHidden = !visibleButtons.contains(.notification)
        menuButton.isHidden = !visibleButtons.contains(.menu)
        settingButton.isHidden = !visibleButtons.contains(.setting)
        
        // 배경 이미지 설정
        if setBgImage == true {
            let appearance = UINavigationBarAppearance()
            appearance.configureWithOpaqueBackground()
            
            if let bgImage = UIImage(named: "navBarBg") {
                appearance.backgroundImage = bgImage
            }
            appearance.shadowColor = .systemGray5
            
            navigationController?.navigationBar.standardAppearance = appearance
            navigationController?.navigationBar.scrollEdgeAppearance = appearance
            searchButton.tintColor = .white
            notificationButton.tintColor = .white
            menuButton.tintColor = .white
        } else {
            // 기본 배경으로 복원
            let appearance = UINavigationBarAppearance()
            appearance.configureWithOpaqueBackground()
            appearance.backgroundColor = .white
            appearance.shadowColor = .systemGray5
            
            navigationController?.navigationBar.standardAppearance = appearance
            navigationController?.navigationBar.scrollEdgeAppearance = appearance
        }
    }

    // MARK: - Button Factory

    private static func makeButton(imageName: String) -> UIButton {
        let button = UIButton(type: .system)
        button.setImage(UIImage(named: imageName), for: .normal)
        button.tintColor = .black
        return button
    }
}
