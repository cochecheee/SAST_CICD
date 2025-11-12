function formatDateFromString(dateString) {
    // Add 'Z' at the end of the string to indicate that dateString is in UTC
    const utcDateString = `${dateString}Z`;
    const date = new Date(utcDateString); // Convert the date string to a Date object
    const now = new Date();
    const diffMs = now.getTime() - date.getTime(); // Difference in milliseconds
    const diffSec = Math.floor(diffMs / 1000); // Difference in seconds
    const diffMin = Math.max(0, Math.floor(diffSec / 60)); // Difference in minutes, ensuring it's not negative
    const diffHour = Math.floor(diffMin / 60); // Difference in hours
    const diffDay = Math.floor(diffHour / 24); // Difference in days

    if (diffMin < 60) {
        return `${diffMin} minutes ago`;
    } else if (diffHour < 24) {
        return `${diffHour} hours ago`;
    } else if (diffDay < 30) {
        return `${diffDay} days ago`;
    } else {
        return new Date(dateString).toLocaleDateString();
    }
}