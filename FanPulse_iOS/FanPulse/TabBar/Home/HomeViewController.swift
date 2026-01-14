//
//  HomeViewController.swift
//  FanPulse
//
//  Created by 김송 on 1/10/26.
//

import UIKit

class HomeViewController: BaseViewController {
    
    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = .white
        
        configureNavigationBar(type: .home)
        setNavigationTitle("FanPulse")
        
        setupUI()
    }
    
    private func setupUI() {
        
    }
}
