//
//  GradientButton.swift
//  FanPulse
//
//  Created by 김송 on 1/18/26.
//

import UIKit

final class GradientButton: UIButton {

    private let gradientLayer = CAGradientLayer()

    init(startColor: UIColor, endColor: UIColor) {
        super.init(frame: .zero)
        gradientLayer.colors = [startColor.cgColor, endColor.cgColor]
        gradientLayer.startPoint = CGPoint(x: 0, y: 0.5)
        gradientLayer.endPoint = CGPoint(x: 1, y: 0.5)
        layer.insertSublayer(gradientLayer, at: 0)
        layer.cornerRadius = 26
        clipsToBounds = true
        setTitleColor(.white, for: .normal)
        titleLabel?.font = .systemFont(ofSize: 16, weight: .semibold)
    }

    override func layoutSubviews() {
        super.layoutSubviews()
        gradientLayer.frame = bounds
    }

    required init?(coder: NSCoder) {
        fatalError()
    }
}
