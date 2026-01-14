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
    
    private let logoutButton: UIButton = {
        let button = UIButton(type: .system)
        button.setTitle("로그아웃", for: .normal)
        button.setTitleColor(.white, for: .normal)
        button.titleLabel?.font = .systemFont(ofSize: 16, weight: .semibold)
        button.backgroundColor = .systemRed
        button.layer.cornerRadius = 12
        return button
    }()
    
    // MARK: - Lifecycle

    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = .systemBackground
        
        configureNavigationBar(type: .my)
        setNavigationTitle("My")
        
        onSettingTapped = {
            self.logoutButtonTapped()
        }
        
        setupUI()
    }
    
    // MARK: - Setup
    
    private func setupUI() {
        
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
