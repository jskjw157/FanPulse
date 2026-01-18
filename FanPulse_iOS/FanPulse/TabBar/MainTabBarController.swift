//
//  MainTabBarController.swift
//  FanPulse
//
//  Created by 김송 on 1/10/26.
//

import UIKit

final class MainTabBarController: UITabBarController {

    override func viewDidLoad() {
        super.viewDidLoad()
        setupTabBar()
        setupViewControllers()
    }

    private func setupTabBar() {
        tabBar.backgroundColor = .systemBackground
        tabBar.tintColor = .systemPurple
        tabBar.unselectedItemTintColor = .systemGray
    }

    private func setupViewControllers() {

        viewControllers = [
            makeTab(
                title: "Home",
                systemImage: "home",
                selectedSystemImage: "home_sel",
                rootViewController: HomeViewController()
            ),
            makeTab(
                title: "Community",
                systemImage: "commu",
                selectedSystemImage: "commu_sel",
                rootViewController: CommunityViewController()
            ),
            makeTab(
                title: "Live",
                systemImage: "live",
                selectedSystemImage: "live_sel",
                rootViewController: LiveViewController()
            ),
            makeTab(
                title: "Voting",
                systemImage: "vote",
                selectedSystemImage: "vote_sel",
                rootViewController:
                    ErrorViewController(
                    title: "문제가 발생했어요",
                    message: "네트워크 오류",
                    description: "인터넷 연결을 확인한 후 다시 시도해주세요.",
                    primaryButtonTitle: "다시 시도",
                    secondaryButtonTitle: "닫기",
                    footerText: "에러 코드: NET_001",
                    primaryAction: {
                        print("다시 시도 버튼 탭")
                    },
                    secondaryAction: {
                        print("닫기 버튼 탭")
                    }
                )
            ),
            makeTab(
                title: "My",
                systemImage: "my",
                selectedSystemImage: "my_sel",
                rootViewController: MyViewController()
            )
        ]
    }

    private func makeTab(
        title: String,
        systemImage: String,
        selectedSystemImage: String,
        rootViewController: UIViewController
    ) -> UIViewController {

        let nav = UINavigationController(rootViewController: rootViewController)
        nav.tabBarItem = UITabBarItem(
            title: title,
            image: UIImage(named: systemImage),
            selectedImage: UIImage(named: selectedSystemImage)
        )
        return nav
    }
}
