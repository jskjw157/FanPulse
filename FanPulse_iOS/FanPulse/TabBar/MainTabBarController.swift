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
                systemImage: "house",
                selectedSystemImage: "house.fill",
                rootViewController: HomeViewController()
            ),
            makeTab(
                title: "Community",
                systemImage: "person.2",
                selectedSystemImage: "person.2.fill",
                rootViewController: CommunityViewController()
            ),
            makeTab(
                title: "Live",
                systemImage: "dot.radiowaves.left.and.right",
                selectedSystemImage: "dot.radiowaves.left.and.right",
                rootViewController: LiveViewController()
            ),
            makeTab(
                title: "Voting",
                systemImage: "checkmark.square",
                selectedSystemImage: "checkmark.square.fill",
                rootViewController: VotingViewController()
            ),
            makeTab(
                title: "My",
                systemImage: "person.circle",
                selectedSystemImage: "person.circle.fill",
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
            image: UIImage(systemName: systemImage),
            selectedImage: UIImage(systemName: selectedSystemImage)
        )
        return nav
    }
}
