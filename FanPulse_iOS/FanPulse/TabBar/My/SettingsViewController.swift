//
//  SettingsViewController.swift
//  FanPulse
//
//  Created by 김송 on 1/12/26.
//

import UIKit

final class SettingsViewController: BaseViewController {

    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = .systemBackground
        title = "Settings"
        configureNavigationBar(type: .def)
        setNavigationBackButton()
    }
}
