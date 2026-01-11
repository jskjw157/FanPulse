import { render, screen } from '@testing-library/react'
import Home from './page'

describe('Home Page Navigation', () => {
  it('renders all essential navigation links', () => {
    render(<Home />)

    // Helper to check link existence
    const checkLink = (href: string) => {
      const link = screen.getAllByRole('link').find(l => l.getAttribute('href')?.startsWith(href))
      expect(link).toBeInTheDocument()
    }

    // Verify main navigation paths
    checkLink('/news-detail')
    checkLink('/live')
    checkLink('/community')
    checkLink('/post-detail')
    checkLink('/chart')
    checkLink('/artist-detail')
    checkLink('/voting')
    checkLink('/concert')
    checkLink('/ads')
    checkLink('/membership')
  })
})
