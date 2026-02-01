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
                        message: "Something went wrong",
                        description: "We're sorry for the inconvenience. The page you're looking for might have been removed or is temporarily unavailable.",
                        secondaryAction: {
                            // Go Back 동작 (옵션)
                        },
                        supportAction: {
                            // 고객 지원 페이지로 이동
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
