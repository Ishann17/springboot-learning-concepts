package com.ishan.user_service.service.user;

import com.ishan.user_service.model.User;
import com.ishan.user_service.repository.UserRepository;
import com.ishan.user_service.utility.CSVReadWriteUtility;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class UserExportServiceImpl implements UserExportService{

    private static final Logger log = LoggerFactory.getLogger(UserExportServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Override
    public Resource exportAllUsersToCSV(String path) {

        // ✅ File-based export (stores CSV on disk and returns it as a Resource)
        // This method is kept for learning and revision purposes.
        // NOTE: This approach is not memory-friendly for huge datasets if we use findAll()
        // because it loads ALL users into RAM at once.
        log.info("File-based CSV export started. TargetPath={}", path);

        long startTime = System.currentTimeMillis();

        // ⚠️ Current implementation loads everything in RAM
        // Works for learning, but may cause high memory usage for millions of rows.
        List<User> userList = userRepository.findAll();

        log.info("Fetched all users from DB for file export. TotalUsers={} FetchTime={}ms",
                userList.size(),
                (System.currentTimeMillis() - startTime));

        Resource resource = CSVReadWriteUtility.writeCSV(userList, path);

        // ✅ Verify resource generation
        if (resource == null || !resource.exists()) {
            log.error("File-based CSV export FAILED. Resource is null OR file not created. Path={}", path);
            return null;
        }

        long endTime = System.currentTimeMillis();
        double executionTimeInSeconds = (endTime - startTime) / 1000.0;

        log.info("File-based CSV export completed successfully. FileName={} TotalUsers={} TotalTime={}s",
                resource.getFilename(),
                userList.size(),
                executionTimeInSeconds);

        return resource;
    }

    /**
     * ✅ Why return type is void?
     *
     * This method does NOT return a Resource / file / List<User> because we are doing STREAMING.
     *
     * In streaming:
     * - Spring gives us an OutputStream (HTTP response body stream)
     * - We write CSV bytes directly into that stream batch-by-batch
     * - The client (browser/Postman) receives data continuously while we are writing
     *
     * So there is nothing to "return" as a final object.
     * The output is the streamed bytes written to the response itself.
     *
     * Real-world analogy (Netflix):
     * - You don’t download the whole movie first and then play it
     * - The movie is streamed chunk-by-chunk to you
     * Same concept here: CSV is streamed chunk-by-chunk.
     */
    @Override
    public void streamUsersAsCsv(OutputStream outputStream) {
        long startTime = System.currentTimeMillis();
        log.info("Started Stream of Users to CSV Export");

        // ✅ Batch size = how many rows we fetch + write in one DB call
        // Bigger batch -> fewer DB trips but more memory per batch
        // Smaller batch -> more DB trips but less memory
        int size = 1000;

        // exportedCount = total rows written to CSV so far
        long exportedCount = 0;

        // We will log progress every N batches
        int logEveryBatches = 10; // 10 batches * 1000 size = 10,000 rows per log

        // We keep "page" variable ONLY as a "batch counter" now
        // Earlier it meant OFFSET pages.
        // Now it just means: how many batches have we processed.
        int page = 0;

        // ✅ Percentage progress requires knowing total records.
        // We run COUNT(*) only once at the beginning (cheap compared to exporting millions).
        long totalUsers = userRepository.countAllUsers();

        log.info("Streaming export setup completed. TotalUsers={} BatchSize={}", totalUsers, size);

        // ✅ WHY BufferedWriter?
        // Earlier approach: outputStream.write(row.getBytes()) for each user -> millions of tiny writes
        // Millions of small writes = very slow due to network & IO overhead
        //
        // BufferedWriter collects multiple writes into an internal buffer and flushes in larger chunks
        // => drastically fewer write operations => much faster streaming
        try (BufferedWriter writer =
                     new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8))) {

            // ✅ Write CSV header once at the very start
            // This ensures the downloaded file always starts with correct column names.
            writer.write(CSVReadWriteUtility.getCSVHeader());

            // ✅ Safety flush after header
            // Ensures client/browser starts receiving content early (especially for huge files)
            writer.flush();

            log.info("CSV header written successfully. Starting batched export...");

            // ✅ KEYSET PAGINATION CURSOR
            // lastUserId works like a "bookmark" / "cursor" in the dataset.
            //
            // Flipkart / Amazon analogy:
            // Instead of saying "skip first 500k orders" (OFFSET),
            // we say "continue after orderId = 500000" (KEYSET)
            Integer lastUserId = 0;

            while (true) {

                // ============================================================
                // ❌ OLD APPROACH: OFFSET PAGINATION (slow for huge exports)
                // ============================================================

                // Pageable pageable = PageRequest.of(page, size);
                //
                // WHY COMMENTED?
                // This creates OFFSET pagination:
                // LIMIT size OFFSET (page * size)
                //
                // Problem:
                // As page increases, OFFSET grows huge.
                // MySQL has to scan/skip more and more rows before returning the next chunk.
                // Example:
                // page=500 -> OFFSET=500,000 (painfully slow)
                //
                // Slice<User> userSlice = userRepository.findAll(pageable);
                //
                // WHY COMMENTED?
                // Slice removed COUNT(*), but it STILL uses OFFSET internally.
                // So performance still degrades as dataset grows.

                // ============================================================
                // ✅ NEW APPROACH: KEYSET PAGINATION (fast + scalable)
                // ============================================================

                // We fetch the "next batch" after lastUserId using:
                // SELECT * FROM user WHERE id > lastUserId ORDER BY id ASC LIMIT size
                //
                // Why fast?
                // DB uses index on id, jumps directly to lastUserId,
                // and reads next "size" rows without scanning/skipping millions.
                List<User> users = userRepository.fetchUserUsingNextBatch(lastUserId, size);

                // If the DB returns empty batch -> we've exported everything
                if (users.isEmpty()) {
                    log.info("No more users found. Export loop completed. lastUserId={} exportedCount={}",
                            lastUserId, exportedCount);
                    break;
                }

                // ✅ Stream current batch rows into response
                // We convert each user into one CSV row and write it directly to output stream.
                for (User user : users) {
                    writer.write(CSVReadWriteUtility.toCsvRow(user));
                    writer.newLine();
                    exportedCount++;
                }

                // ✅ Move cursor forward ONCE per batch
                // lastUserId becomes the last exported user's id,
                // so next loop continues after it.
                lastUserId = users.getLast().getId();

                // ✅ Flush once per batch (NOT per row)
                // Why:
                // - per-row flush would kill performance (too many flush calls)
                // - per-batch flush ensures client receives data continuously
                writer.flush();

                // ✅ Clear persistence context once per batch
                // Even though we are not updating entities, Hibernate still tracks them.
                // If we don't clear, memory usage can grow across millions of rows.
                entityManager.clear();

                // Increment batch counter (we are reusing "page" only for progress logging)
                page++;

                // ============================================================
                // ✅ Progress Logging (with percentage)
                // ============================================================
                if (page % logEveryBatches == 0) {
                    long now = System.currentTimeMillis();
                    double elapsedSeconds = (now - startTime) / 1000.0;

                    // Percentage completion based on COUNT(*) at start
                    // Note: This assumes no major deletes/inserts during export (usually fine)
                    double percent = totalUsers == 0 ? 100.0 : (exportedCount * 100.0) / totalUsers;

                    log.info(
                            "Progress: batch={} exported={}/{} ({}%) lastUserId={} elapsed={}s",
                            page,
                            exportedCount,
                            totalUsers,
                            String.format("%.2f", percent),
                            lastUserId,
                            elapsedSeconds
                    );
                }
            }

        } catch (IOException exception) {
            // ✅ IOException can occur if client cancels the download mid-way
            // OR network breaks, OR browser closes.
            log.error("Error while streaming CSV", exception);
        } finally {
            long endTime = System.currentTimeMillis();
            double executionTimeInSeconds = (endTime - startTime) / 1000.0;

            // ✅ Always log completion time even if export failed mid-way
            log.info("Stream of Users to CSV Export completed in {} seconds", executionTimeInSeconds);
        }
    }
}
