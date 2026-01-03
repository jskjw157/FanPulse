//
//  SceneDelegate.swift
//  FanPulse
//
//  Created by 김송 on 12/28/25.
//

import UIKit
import GoogleSignIn

class SceneDelegate: UIResponder, UIWindowSceneDelegate {

    var window: UIWindow?

    func scene(
        _ scene: UIScene,
        willConnectTo session: UISceneSession,
        options connectionOptions: UIScene.ConnectionOptions
    ) {
        guard let windowScene = scene as? UIWindowScene else { return }

        let window = UIWindow(windowScene: windowScene)
        self.window = window

        if KeychainManager.shared.isLoggedIn() {
            restorePreviousSignIn()
        } else {
            showLoginScreen()
        }

        window.makeKeyAndVisible()
    }

    // MARK: - Login Restore

    private func restorePreviousSignIn() {
        GIDSignIn.sharedInstance.restorePreviousSignIn { user, error in
            if let error = error {
                print("자동 로그인 실패: \(error.localizedDescription)")
                self.showLoginScreen()
                return
            }

            if let user = user {
                print("자동 로그인 성공: \(user.profile?.email ?? "")")
            }

            self.showMainScreen()
        }
    }

    // MARK: - Root Switch

    private func showLoginScreen() {
        DispatchQueue.main.async {
            let loginVC = AuthViewController()
            let nav = UINavigationController(rootViewController: loginVC)
            self.window?.rootViewController = nav
        }
    }

    private func showMainScreen() {
        DispatchQueue.main.async {
            print("📱 메인 화면 표시")

            let mainVC = ViewController()
            let nav = UINavigationController(rootViewController: mainVC)

            self.window?.rootViewController = nav
        }
    }
}
