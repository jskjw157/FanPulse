import { render, screen, fireEvent } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, it, expect, vi } from 'vitest';
import { SearchBar } from './SearchBar';

describe('SearchBar', () => {
  const defaultProps = {
    value: '',
    onChange: vi.fn(),
    onSearch: vi.fn(),
    onClear: vi.fn(),
  };

  it('renders input with placeholder', () => {
    render(<SearchBar {...defaultProps} placeholder="검색어 입력" />);
    expect(screen.getByPlaceholderText('검색어 입력')).toBeInTheDocument();
  });

  it('calls onChange when typing', async () => {
    const onChange = vi.fn();
    render(<SearchBar {...defaultProps} onChange={onChange} />);

    const input = screen.getByRole('textbox');
    await userEvent.type(input, 'BTS');

    expect(onChange).toHaveBeenCalledWith('B');
    expect(onChange).toHaveBeenCalledWith('T');
    expect(onChange).toHaveBeenCalledWith('S');
  });

  it('calls onSearch on Enter key when value >= 2 chars', async () => {
    const onSearch = vi.fn();
    render(<SearchBar {...defaultProps} value="BTS" onSearch={onSearch} />);

    const input = screen.getByRole('textbox');
    await userEvent.type(input, '{enter}');

    expect(onSearch).toHaveBeenCalledWith('BTS');
  });

  it('does not call onSearch on Enter when value < 2 chars', async () => {
    const onSearch = vi.fn();
    render(<SearchBar {...defaultProps} value="B" onSearch={onSearch} />);

    const input = screen.getByRole('textbox');
    await userEvent.type(input, '{enter}');

    expect(onSearch).not.toHaveBeenCalled();
  });

  it('calls onSearch when search button clicked', async () => {
    const onSearch = vi.fn();
    render(<SearchBar {...defaultProps} value="BTS" onSearch={onSearch} />);

    const searchButton = screen.getByRole('button', { name: /검색/i });
    await userEvent.click(searchButton);

    expect(onSearch).toHaveBeenCalledWith('BTS');
  });

  it('disables search button when value < minLength', () => {
    render(<SearchBar {...defaultProps} value="B" minLength={2} />);

    const searchButton = screen.getByRole('button', { name: /검색/i });
    expect(searchButton).toBeDisabled();
  });

  it('shows clear button when value exists', () => {
    render(<SearchBar {...defaultProps} value="BTS" />);
    expect(screen.getByRole('button', { name: /지우기/i })).toBeInTheDocument();
  });

  it('hides clear button when value is empty', () => {
    render(<SearchBar {...defaultProps} value="" />);
    expect(
      screen.queryByRole('button', { name: /지우기/i })
    ).not.toBeInTheDocument();
  });

  it('calls onClear when clear button clicked', async () => {
    const onClear = vi.fn();
    render(<SearchBar {...defaultProps} value="BTS" onClear={onClear} />);

    const clearButton = screen.getByRole('button', { name: /지우기/i });
    await userEvent.click(clearButton);

    expect(onClear).toHaveBeenCalled();
  });

  it('auto-focuses input on mount', () => {
    render(<SearchBar {...defaultProps} autoFocus />);
    expect(screen.getByRole('textbox')).toHaveFocus();
  });
});
