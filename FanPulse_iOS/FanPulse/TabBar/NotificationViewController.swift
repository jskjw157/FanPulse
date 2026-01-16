//
//  NotificationViewController.swift
//  FanPulse
//
//  Created by 김송 on 1/16/26.
//

import UIKit

final class NotificationViewController: BaseViewController {

    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = .systemBackground
        title = "Noti"
        configureNavigationBar(type: .noti)
        setNavigationBackButton()
    }
}
