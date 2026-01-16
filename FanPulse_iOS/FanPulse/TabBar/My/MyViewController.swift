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
    
    private let profileBackgroundImageView: UIImageView = {
        let imageView = UIImageView()
        imageView.contentMode = .scaleAspectFill
        imageView.clipsToBounds = true
        imageView.image = UIImage(named: "myPageBg")
        return imageView
    }()
    
    private let pointBackgroundView: UIView = {
        let view = UIView()
        view.backgroundColor = .white
        view.layer.cornerRadius = 16
        return view
    }()
    
    private let myListBackgroundView: UIView = {
        let view = UIView()
        view.backgroundColor = .white
        view.layer.cornerRadius = 16
        return view
    }()
    
    private let logoutButtonView: UIView = {
        let view = UIView()
        view.backgroundColor = .white
        view.layer.cornerRadius = 16
        return view
    }()
    
    private let logoutButtonLabel: UILabel = {
        let label = UILabel()
        label.font = .systemFont(ofSize: 14, weight: .medium)
        label.textAlignment = .center
        label.text = "로그아웃"
        label.isUserInteractionEnabled = false
        return label
    }()
    
    // MARK: - Lifecycle

    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = .systemBackground
        
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
        
        setupContentView()
    }

    private func setupContentView() {
        contentView.addSubview(profileBackgroundImageView)
        contentView.addSubview(pointBackgroundView)
        contentView.addSubview(myListBackgroundView)
        contentView.addSubview(logoutButtonView)
        logoutButtonView.addSubview(logoutButtonLabel)
        
        profileBackgroundImageView.snp.makeConstraints { make in
            make.leading.trailing.top.equalToSuperview()
            make.height.equalTo(244)
        }
        
        pointBackgroundView.snp.makeConstraints { make in
            make.top.equalTo(profileBackgroundImageView.snp.bottom).offset(16)
            make.horizontalEdges.equalToSuperview().inset(16)
            make.height.equalTo(246)
        }
        
        myListBackgroundView.snp.makeConstraints { make in
            make.top.equalTo(pointBackgroundView.snp.bottom).offset(16)
            make.horizontalEdges.equalToSuperview().inset(16)
            make.height.equalTo(464)
        }
        
        logoutButtonView.snp.makeConstraints { make in
            make.top.equalTo(myListBackgroundView.snp.bottom).offset(16)
            make.horizontalEdges.equalToSuperview().inset(16)
            make.height.equalTo(47)
            make.bottom.equalToSuperview().inset(16)
        }
        
        logoutButtonLabel.snp.makeConstraints { make in
            make.center.equalToSuperview()
        }
    }
    
    // MARK: - Actions
    
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
        // TODO: 로그아웃 로직 구현
        print("로그아웃 처리")
        KeychainManager.shared.deleteAllTokens()
        // 예시: 로그인 화면으로 이동
         let loginVC = LoginViewController()
         let nav = UINavigationController(rootViewController: loginVC)
         nav.modalPresentationStyle = .fullScreen
         present(nav, animated: true)
    }
}
