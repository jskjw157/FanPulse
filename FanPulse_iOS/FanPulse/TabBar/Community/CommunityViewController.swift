//
//  CommunityViewController.swift
//  FanPulse
//
//  Created by 김송 on 1/10/26.
//

import UIKit

final class CommunityViewController: BaseViewController {
    
    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = .white
        
        configureNavigationBar(type: .commu)
        setNavigationTitle("Community")
        
        setupUI()
    }
    
    private func setupUI() {
        
    }
}
