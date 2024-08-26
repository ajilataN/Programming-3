import matplotlib.pyplot as plt
import numpy as np
import pandas as pd

modes = ['parallel', 'sequential', 'distributed']
colors = ['g', 'b', 'r']

def analyze_reviews(file):
    reviews_per_second = []

    with open(file, 'r') as f:
        for line in f:
            if "Analyzed Reviews per Second:" in line:
                    reviews_per_second.append(int(line.split(":")[1].strip()))

    return reviews_per_second

def compute_statistics(reviews_per_second):
    total_reviews = sum(reviews_per_second)
    average_reviews = np.mean(reviews_per_second)
    max_reviews = np.max(reviews_per_second)
    median_reviews = np.median(reviews_per_second)
    min_reviews = np.min(reviews_per_second)
    std_dev = np.std(reviews_per_second)
    percentage_zero = (reviews_per_second.count(0) / len(reviews_per_second)) * 100

    return {
        'Total': total_reviews,
        'Average': average_reviews,
        'Max': max_reviews,
        'Median': median_reviews,
        'Min': min_reviews,
        'Standard Deviation': std_dev,
        'Percentage Zero Reviews': percentage_zero,
    }

def plot_analysis(modes, colors):
    plt.figure(figsize=(12, 8))

    for mode, color in zip(modes, colors):
        filename = mode + '_reviews_per_second.txt'
        reviews_per_second = analyze_reviews(filename)
        time = range(1, len(reviews_per_second) + 1)

        plt.plot(time, reviews_per_second, marker='o', linestyle='-', color=color, label=mode.capitalize())

    plt.title('Reviews Analyzed Per Second - Comparison of Implementations')
    plt.xlabel('Time (s)')
    plt.ylabel('Analyzed Reviews')
    plt.grid(True)
    plt.legend()
    plt.tight_layout()

    plt.show()

def plot_statistics(stats):
    labels = list(stats.keys())
    values = list(stats.values())

    plt.figure(figsize=(8, 6))

    plt.barh(labels, values, color='skyblue')
    plt.title('Summary of Analyzed Reviews Statistics')

    # Label the values on the bars
    for i, v in enumerate(values):
        plt.text(v + 1, i, f'{v:.2f}' if isinstance(v, float) else f'{v}', color='black', va='center')

    # Show the chart
    plt.tight_layout()
    plt.show()

def plot_heatmap(modes):
    plt.figure(figsize=(12, 8))

    for i, mode in enumerate(modes):
        filename = mode + '_reviews_per_second.txt'
        reviews_per_second = analyze_reviews(filename)

        # Create a histogram
        plt.hist(reviews_per_second, bins=50, alpha=0.5, color=colors[i], label=mode.capitalize(), density=True)

    plt.title('Heatmap of Analyzed Reviews Per Second')
    plt.xlabel('Analyzed Reviews')
    plt.ylabel('Density')
    plt.legend()
    plt.grid(True)
    plt.tight_layout()

    plt.show()

def plot_boxplot(modes):
    data = []
    for mode in modes:
        filename = mode + '_reviews_per_second.txt'
        reviews_per_second = analyze_reviews(filename)
        data.append(reviews_per_second)

    plt.figure(figsize=(12, 8))
    plt.boxplot(data, labels=[mode.capitalize() for mode in modes])
    plt.title('Box Plot of Analyzed Reviews Per Second')
    plt.xlabel('Implementation')
    plt.ylabel('Analyzed Reviews')
    plt.grid(True)
    plt.tight_layout()

    plt.show()

def plot_analysis2(modes, colors):
    plt.figure(figsize=(12, 8))

    for mode, color in zip(modes, colors):
        filename = mode + '_reviews_per_second.txt'
        reviews_per_second = analyze_reviews(filename)
        time = range(1, len(reviews_per_second) + 1)

        # Convert to DataFrame for easier rolling mean calculation
        df = pd.DataFrame({'Time': time, 'Reviews': reviews_per_second})
        df['Rolling_Avg'] = df['Reviews'].rolling(window=50).mean()  # 50-point rolling average

        # Plot original data
        # plt.plot(df['Time'], df['Reviews'], color=color, alpha=0.5, label=f'{mode.capitalize()} (Raw)')
        # Plot smoothed data
        plt.plot(df['Time'], df['Rolling_Avg'], color=color, linestyle='--', label=f'{mode.capitalize()} (Smoothed)')

    plt.title('Reviews Analyzed Per Second - Comparison of Implementations')
    plt.xlabel('Time (s)')
    plt.ylabel('Analyzed Reviews')
    plt.grid(True)
    plt.legend()
    plt.tight_layout()

    plt.show()

def plot_statistics_comparison(modes, colors):
    # Prepare data
    stats_all = {}
    for mode in modes:
        filename = mode + '_reviews_per_second.txt'
        reviews_per_second = analyze_reviews(filename)
        stats_all[mode] = compute_statistics(reviews_per_second)

    # Create subplots
    fig, axes = plt.subplots(2, 4, figsize=(18, 10), sharey='row')
    fig.suptitle('Comparison of Statistics Across Implementations', fontsize=16)

    # Define the statistics we are plotting
    statistics = ['Total', 'Average', 'Max', 'Median', 'Min', 'Standard Deviation', 'Percentage Zero Reviews']

    for i, stat in enumerate(statistics):
        values = [stats_all[mode].get(stat, 0) for mode in modes]
        axes[i // 4, i % 4].bar(modes, values, color=colors)
        axes[i // 4, i % 4].set_title(stat)
        axes[i // 4, i % 4].set_ylabel('Value')
        axes[i // 4, i % 4].grid(True)

    plt.tight_layout(rect=[0, 0, 1, 0.96])
    plt.show()

if __name__ == '__main__':
    plot_analysis(modes, colors)
    plot_heatmap(modes)
    plot_boxplot(modes)
    plot_analysis2(modes, colors)
    plot_statistics_comparison(modes, colors)

    for mode in modes:
        filename = mode + '_reviews_per_second.txt'
        reviews_per_second = analyze_reviews(filename)

        stats = compute_statistics(reviews_per_second)
        # plot_statistics(stats)

        print(f"Statistics for {mode.capitalize()}:")
        for key, value in stats.items():
            print(f"{key}: {value:.2f}" if isinstance(value, float) else f"{key}: {value}")
        print()
