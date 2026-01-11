//
//  FadeToBackgroundGradientView.swift
//  FanPulse
//
//  Created by 김송 on 12/28/25.
//

import UIKit
import SnapKit

class FadeToBackgroundGradientView: UIView {
    
    private let gradientLayer = CAGradientLayer()
    
    var baseColor: UIColor = .white {
        didSet { updateGradient() }
    }
    
    var fadeColor: UIColor = .black {
        didSet { updateGradient() }
    }
    
    var startPoint: CGPoint = CGPoint(x: 0, y: 0.5) {
        didSet { updateGradient() }
    }
    
    var endPoint: CGPoint = CGPoint(x: 1, y: 0.5) {
        didSet { updateGradient() }
    }
    
    init(baseColor: UIColor, fadeColor: UIColor, startPoint: CGPoint = CGPoint(x: 0, y: 0.5), endPoint: CGPoint = CGPoint(x: 1, y: 0.5)) {
        self.baseColor = baseColor
        self.fadeColor = fadeColor
        self.startPoint = startPoint
        self.endPoint = endPoint
        super.init(frame: .zero)
        setupGradient()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        gradientLayer.frame = bounds
    }
    
    private func setupGradient() {
        layer.insertSublayer(gradientLayer, at: 0)
        updateGradient()
    }
    
    private func updateGradient() {
        gradientLayer.colors = [
            fadeColor.cgColor,
            fadeColor.withAlphaComponent(0.7).cgColor,
            fadeColor.withAlphaComponent(0.3).cgColor,
            fadeColor.withAlphaComponent(0).cgColor,
            fadeColor.withAlphaComponent(0.3).cgColor,
            fadeColor.withAlphaComponent(0.7).cgColor,
            fadeColor.cgColor
        ]
        
        gradientLayer.startPoint = startPoint
        gradientLayer.endPoint = endPoint
        self.backgroundColor = baseColor
    }
}
