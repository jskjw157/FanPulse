//
//  LiveViewController.swift
//  FanPulse
//
//  Created by 김송 on 1/10/26.
//

import UIKit

final class LiveViewController: BaseViewController {
    
    override func viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = .white
        
        configureNavigationBar(type: .live)
        setNavigationTitle("Live")
        
        setupUI()
    }
    
    private func setupUI() {
        
    }
}
