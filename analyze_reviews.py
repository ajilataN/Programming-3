import matplotlib.pyplot as plt
import numpy as np

mode = 'sequential'  # or 'parallel'

# Read the data from the file
filename = mode+'_reviews_per_second.txt'
def analyze_reviews(file):
    # Read the file and extract the reviews per second
    reviews_per_second = []

    with open(file, 'r') as f:
        for line in f:
            if "Analyzed Reviews per Second:" in line:
                # Extract the numeric value
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

def plot_analysis(reviews_per_second):
    time = range(1, len(reviews_per_second) + 1)

    plt.figure(figsize=(12, 10))

    # Line plot for Reviews per Second
    plt.subplot(1, 1, 1)
    plt.plot(time, reviews_per_second, marker='o', linestyle='-', color='b')
    plt.title('Reviews Analyzed Per Second - '+mode+' implementation')
    plt.xlabel('Time (s)')
    plt.ylabel('Analyzed Reviews')
    plt.grid(True)

    # # Bar plot for Reviews per Second
    # plt.subplot(2, 1, 2)
    # plt.bar(time, reviews_per_second, color='orange')
    # plt.title('Reviews Analyzed Per Second (Bar Chart)')
    # plt.xlabel('Time (s)')
    # plt.ylabel('Analyzed Reviews')

    # Adjust layout
    plt.tight_layout()

    # Show the plots
    plt.show()

def plot_statistics(stats):
    # Prepare data for the statistics chart
    labels = list(stats.keys())
    values = list(stats.values())

    plt.figure(figsize=(8, 6))

    # Create a bar chart for the statistics
    plt.barh(labels, values, color='skyblue')
    plt.title('Summary of Analyzed Reviews Statistics')

    # Label the values on the bars
    for i, v in enumerate(values):
        plt.text(v + 1, i, f'{v:.2f}' if isinstance(v, float) else f'{v}', color='black', va='center')

    # Show the chart
    plt.tight_layout()
    plt.show()

# MainSequentialAndParallel function to run the analysis
if __name__ == '__main__':
    reviews_per_second = analyze_reviews(filename)

    # Compute statistics
    stats = compute_statistics(reviews_per_second)

    # Print statistics to the console
    print("Statistics:")
    for key, value in stats.items():
        print(f"{key}: {value:.2f}" if isinstance(value, float) else f"{key}: {value}")


    # Generate the analysis plots
    plot_analysis(reviews_per_second)

    # # Generate the statistics plot
    # plot_statistics(stats)