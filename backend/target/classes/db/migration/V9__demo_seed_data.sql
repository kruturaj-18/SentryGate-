-- V9__demo_seed_data.sql
-- Seed data for demo mode. Only inserted if no repositories exist.

DO $$
BEGIN
  -- Insert demo admin user
  IF NOT EXISTS (SELECT 1 FROM users WHERE github_id = 1) THEN
    INSERT INTO users (github_id, login, name, email, avatar_url)
    VALUES
      (1, 'demo-admin', 'Demo Admin', 'admin@sentrygate.io', 'https://avatars.githubusercontent.com/u/1?v=4'),
      (2, 'alice-dev', 'Alice Developer', 'alice@example.com', 'https://avatars.githubusercontent.com/u/2?v=4'),
      (3, 'john-dev', 'John Developer', 'john@example.com', 'https://avatars.githubusercontent.com/u/3?v=4'),
      (4, 'rahul-dev', 'Rahul Developer', 'rahul@example.com', 'https://avatars.githubusercontent.com/u/4?v=4');

    -- Assign roles
    INSERT INTO user_roles (user_id, role_id)
    SELECT u.id, r.id FROM users u, roles r
    WHERE u.login = 'demo-admin' AND r.name = 'ROLE_ADMIN';

    INSERT INTO user_roles (user_id, role_id)
    SELECT u.id, r.id FROM users u, roles r
    WHERE u.login IN ('alice-dev','john-dev','rahul-dev') AND r.name = 'ROLE_MAINTAINER';

    -- Demo GitHub installation
    INSERT INTO github_installations (installation_id, account_login, account_type, installed_by_user_id)
    SELECT 12345678, 'demo-org', 'Organization', id FROM users WHERE login = 'demo-admin';

    -- Demo repositories
    INSERT INTO repositories (github_repo_id, name, full_name, owner, url, html_url, description, default_branch, private_repo, language, installation_id, connected_user_id, monitoring_status, open_pr_count, open_issue_count)
    SELECT
      101, 'sentrygate-demo', 'demo-org/sentrygate-demo', 'demo-org',
      'https://api.github.com/repos/demo-org/sentrygate-demo',
      'https://github.com/demo-org/sentrygate-demo',
      'Main SentryGate demo repository', 'main', false, 'JavaScript',
      gi.id, u.id, 'ACTIVE', 4, 12
    FROM github_installations gi, users u
    WHERE gi.installation_id = 12345678 AND u.login = 'demo-admin';

    INSERT INTO repositories (github_repo_id, name, full_name, owner, url, html_url, description, default_branch, private_repo, language, installation_id, connected_user_id, monitoring_status, open_pr_count, open_issue_count)
    SELECT
      102, 'task-manager', 'demo-org/task-manager', 'demo-org',
      'https://api.github.com/repos/demo-org/task-manager',
      'https://github.com/demo-org/task-manager',
      'Task management application backend', 'main', false, 'Java',
      gi.id, u.id, 'ACTIVE', 2, 8
    FROM github_installations gi, users u
    WHERE gi.installation_id = 12345678 AND u.login = 'demo-admin';

    INSERT INTO repositories (github_repo_id, name, full_name, owner, url, html_url, description, default_branch, private_repo, language, installation_id, connected_user_id, monitoring_status, open_pr_count, open_issue_count)
    SELECT
      103, 'ecommerce-api', 'demo-org/ecommerce-api', 'demo-org',
      'https://api.github.com/repos/demo-org/ecommerce-api',
      'https://github.com/demo-org/ecommerce-api',
      'E-commerce platform REST API', 'develop', true, 'Java',
      gi.id, u.id, 'ACTIVE', 3, 15
    FROM github_installations gi, users u
    WHERE gi.installation_id = 12345678 AND u.login = 'demo-admin';

    -- Default rules for first repository
    INSERT INTO repository_rules (repository_id, rule_code, rule_name, enabled, severity, configuration)
    SELECT r.id,
      unnest(ARRAY['PR_TITLE_FORMAT','PR_DESCRIPTION_LENGTH','REQUIRED_FILES','BRANCH_NAMING','LARGE_CHANGE_DETECTION','TEST_PRESENCE','SECRET_DETECTION','STATIC_ANALYSIS']),
      unnest(ARRAY['PR Title Format','PR Description Length','Required Files Check','Branch Naming Convention','Large Change Detection','Test Presence Check','Secret Detection','Basic Static Analysis']),
      true,
      unnest(ARRAY['MEDIUM','LOW','HIGH','LOW','MEDIUM','MEDIUM','CRITICAL','LOW']::VARCHAR[]),
      unnest(ARRAY[
        '{"pattern":"^(feat|fix|docs|refactor|test|chore|style|perf)(\\\\(.*\\\\))?:\\\\s.+$","message":"Use conventional commits format"}',
        '{"minLength":50}',
        '{"requiredFiles":["README.md","LICENSE"]}',
        '{"allowedPatterns":["feature/.*","bugfix/.*","hotfix/.*","main","develop"]}',
        '{"maxAdditions":500,"maxDeletions":300}',
        '{"requireTests":true,"testPatterns":["*.test.*","*.spec.*","*Test.*","*Tests.*"]}',
        '{}',
        '{}'
      ]::JSONB[])
    FROM repositories r WHERE r.github_repo_id = 101;

    -- Demo pull requests
    INSERT INTO pull_requests (github_pr_id, number, title, body, author_login, author_github_id, head_branch, base_branch, head_sha, state, additions, deletions, changed_files, html_url, repository_id, github_created_at, github_updated_at)
    SELECT
      1001, 42, 'feat(auth): Add GitHub OAuth authentication',
      'This PR implements complete GitHub OAuth flow with JWT session management. Includes login, callback, and logout endpoints. Tests are included.',
      'alice-dev', 2, 'feature/oauth-auth', 'main', 'abc123def456',
      'open', 145, 23, 12,
      'https://github.com/demo-org/sentrygate-demo/pull/42',
      r.id, NOW() - INTERVAL '2 days', NOW() - INTERVAL '1 hour'
    FROM repositories r WHERE r.github_repo_id = 101;

    INSERT INTO pull_requests (github_pr_id, number, title, body, author_login, author_github_id, head_branch, base_branch, head_sha, state, additions, deletions, changed_files, html_url, repository_id, github_created_at, github_updated_at)
    SELECT
      1002, 41, 'fix(db): Resolve database connection pool exhaustion',
      'Fixes the connection pool issue under high load. Increased pool size and added connection timeout configuration.',
      'john-dev', 3, 'bugfix/db-connection', 'main', 'def456ghi789',
      'open', 34, 8, 3,
      'https://github.com/demo-org/sentrygate-demo/pull/41',
      r.id, NOW() - INTERVAL '3 days', NOW() - INTERVAL '5 hours'
    FROM repositories r WHERE r.github_repo_id = 101;

    INSERT INTO pull_requests (github_pr_id, number, title, body, author_login, author_github_id, head_branch, base_branch, head_sha, state, additions, deletions, changed_files, html_url, repository_id, github_created_at, github_updated_at)
    SELECT
      1003, 40, 'docs: Update API documentation',
      'Updated OpenAPI documentation for all endpoints. Added examples and improved descriptions.',
      'rahul-dev', 4, 'feature/update-docs', 'main', 'ghi789jkl012',
      'closed', 89, 45, 8,
      'https://github.com/demo-org/sentrygate-demo/pull/40',
      r.id, NOW() - INTERVAL '7 days', NOW() - INTERVAL '6 days'
    FROM repositories r WHERE r.github_repo_id = 101;

    INSERT INTO pull_requests (github_pr_id, number, title, body, author_login, author_github_id, head_branch, base_branch, head_sha, state, additions, deletions, changed_files, html_url, repository_id, github_created_at, github_updated_at)
    SELECT
      1004, 39, 'Add payment module integration',
      NULL,
      'john-dev', 3, 'payment-integration', 'main', 'jkl012mno345',
      'open', 623, 12, 25,
      'https://github.com/demo-org/sentrygate-demo/pull/39',
      r.id, NOW() - INTERVAL '1 day', NOW() - INTERVAL '30 minutes'
    FROM repositories r WHERE r.github_repo_id = 101;

    -- Validation for PR #42 (PASSED)
    WITH val AS (
      INSERT INTO pull_request_validations (pull_request_id, overall_status, pass_count, fail_count, warning_count, skipped_count, triggered_by, completed_at)
      SELECT pr.id, 'PASSED', 7, 0, 1, 0, 'WEBHOOK', NOW() - INTERVAL '1 hour'
      FROM pull_requests pr WHERE pr.number = 42 AND pr.repository_id = (SELECT id FROM repositories WHERE github_repo_id = 101)
      RETURNING id
    )
    INSERT INTO validation_results (validation_id, rule_code, rule_name, status, severity, message)
    SELECT val.id, rc, rn, rs, rv, rm FROM val,
    (VALUES
      ('PR_TITLE_FORMAT','PR Title Format','PASS','MEDIUM','Title follows conventional commits format'),
      ('PR_DESCRIPTION_LENGTH','PR Description Length','PASS','LOW','Description has 147 characters (min: 50)'),
      ('REQUIRED_FILES','Required Files Check','PASS','HIGH','All required files present: README.md, LICENSE'),
      ('BRANCH_NAMING','Branch Naming Convention','PASS','LOW','Branch ''feature/oauth-auth'' matches allowed pattern ''feature/.*'''),
      ('LARGE_CHANGE_DETECTION','Large Change Detection','WARNING','MEDIUM','PR has 145 additions. Consider splitting if unrelated changes exist.'),
      ('TEST_PRESENCE','Test Presence Check','PASS','MEDIUM','Test files detected in changeset'),
      ('SECRET_DETECTION','Secret Detection','PASS','CRITICAL','No potential secrets detected'),
      ('STATIC_ANALYSIS','Basic Static Analysis','PASS','LOW','No obvious code quality issues detected')
    ) AS t(rc, rn, rs, rv, rm);

    -- Validation for PR #41 (PASSED)
    WITH val AS (
      INSERT INTO pull_request_validations (pull_request_id, overall_status, pass_count, fail_count, warning_count, skipped_count, triggered_by, completed_at)
      SELECT pr.id, 'PASSED', 8, 0, 0, 0, 'WEBHOOK', NOW() - INTERVAL '5 hours'
      FROM pull_requests pr WHERE pr.number = 41 AND pr.repository_id = (SELECT id FROM repositories WHERE github_repo_id = 101)
      RETURNING id
    )
    INSERT INTO validation_results (validation_id, rule_code, rule_name, status, severity, message)
    SELECT val.id, rc, rn, rs, rv, rm FROM val,
    (VALUES
      ('PR_TITLE_FORMAT','PR Title Format','PASS','MEDIUM','Title follows conventional commits format'),
      ('PR_DESCRIPTION_LENGTH','PR Description Length','PASS','LOW','Description has 89 characters (min: 50)'),
      ('REQUIRED_FILES','Required Files Check','PASS','HIGH','All required files present'),
      ('BRANCH_NAMING','Branch Naming Convention','PASS','LOW','Branch ''bugfix/db-connection'' matches pattern'),
      ('LARGE_CHANGE_DETECTION','Large Change Detection','PASS','MEDIUM','34 additions are within limits'),
      ('TEST_PRESENCE','Test Presence Check','PASS','MEDIUM','Test files detected'),
      ('SECRET_DETECTION','Secret Detection','PASS','CRITICAL','No secrets detected'),
      ('STATIC_ANALYSIS','Basic Static Analysis','PASS','LOW','No issues detected')
    ) AS t(rc, rn, rs, rv, rm);

    -- Validation for PR #39 (FAILED - multiple issues)
    WITH val AS (
      INSERT INTO pull_request_validations (pull_request_id, overall_status, pass_count, fail_count, warning_count, skipped_count, triggered_by, completed_at)
      SELECT pr.id, 'FAILED', 3, 3, 1, 1, 'WEBHOOK', NOW() - INTERVAL '30 minutes'
      FROM pull_requests pr WHERE pr.number = 39 AND pr.repository_id = (SELECT id FROM repositories WHERE github_repo_id = 101)
      RETURNING id
    )
    INSERT INTO validation_results (validation_id, rule_code, rule_name, status, severity, message)
    SELECT val.id, rc, rn, rs, rv, rm FROM val,
    (VALUES
      ('PR_TITLE_FORMAT','PR Title Format','FAIL','MEDIUM','Title does not follow conventional commits format. Expected: feat|fix|docs|...'),
      ('PR_DESCRIPTION_LENGTH','PR Description Length','FAIL','LOW','Description is empty. Minimum length is 50 characters.'),
      ('REQUIRED_FILES','Required Files Check','PASS','HIGH','All required files present'),
      ('BRANCH_NAMING','Branch Naming Convention','FAIL','LOW','Branch ''payment-integration'' does not match any allowed pattern'),
      ('LARGE_CHANGE_DETECTION','Large Change Detection','WARNING','MEDIUM','PR has 623 additions, exceeding limit of 500. Consider splitting this PR.'),
      ('TEST_PRESENCE','Test Presence Check','PASS','MEDIUM','Test files detected'),
      ('SECRET_DETECTION','Secret Detection','PASS','CRITICAL','No secrets detected'),
      ('STATIC_ANALYSIS','Basic Static Analysis','SKIPPED','LOW','Skipped due to earlier critical failures')
    ) AS t(rc, rn, rs, rv, rm);

    -- Demo contributors
    INSERT INTO contributors (github_id, login, name, avatar_url, repository_id, total_prs, merged_prs, rejected_prs, validation_passes, validation_failures, contribution_count, first_contribution_at, last_contribution_at)
    SELECT 2, 'alice-dev', 'Alice Developer', 'https://avatars.githubusercontent.com/u/2?v=4',
      r.id, 18, 15, 1, 42, 3, 18, NOW() - INTERVAL '180 days', NOW() - INTERVAL '1 day'
    FROM repositories r WHERE r.github_repo_id = 101;

    INSERT INTO contributors (github_id, login, name, avatar_url, repository_id, total_prs, merged_prs, rejected_prs, validation_passes, validation_failures, contribution_count, first_contribution_at, last_contribution_at)
    SELECT 3, 'john-dev', 'John Developer', 'https://avatars.githubusercontent.com/u/3?v=4',
      r.id, 12, 9, 2, 28, 8, 12, NOW() - INTERVAL '120 days', NOW() - INTERVAL '3 days'
    FROM repositories r WHERE r.github_repo_id = 101;

    INSERT INTO contributors (github_id, login, name, avatar_url, repository_id, total_prs, merged_prs, rejected_prs, validation_passes, validation_failures, contribution_count, first_contribution_at, last_contribution_at)
    SELECT 4, 'rahul-dev', 'Rahul Developer', 'https://avatars.githubusercontent.com/u/4?v=4',
      r.id, 8, 6, 1, 18, 5, 8, NOW() - INTERVAL '90 days', NOW() - INTERVAL '6 days'
    FROM repositories r WHERE r.github_repo_id = 101;

    -- Trust Scores
    INSERT INTO trust_scores (contributor_id, overall_score, pr_success_score, validation_score, collaboration_score, consistency_score)
    SELECT c.id, 88.5, 91.2, 87.3, 84.0, 91.5 FROM contributors c WHERE c.login = 'alice-dev';

    INSERT INTO trust_scores (contributor_id, overall_score, pr_success_score, validation_score, collaboration_score, consistency_score)
    SELECT c.id, 75.2, 77.8, 70.0, 76.5, 76.5 FROM contributors c WHERE c.login = 'john-dev';

    INSERT INTO trust_scores (contributor_id, overall_score, pr_success_score, validation_score, collaboration_score, consistency_score)
    SELECT c.id, 81.0, 83.5, 78.2, 80.0, 82.3 FROM contributors c WHERE c.login = 'rahul-dev';

    -- Trust history
    INSERT INTO trust_score_history (contributor_id, overall_score, recorded_at)
    SELECT c.id, score, ts FROM contributors c,
    (VALUES
      ('alice-dev', 72.0, NOW() - INTERVAL '90 days'),
      ('alice-dev', 76.5, NOW() - INTERVAL '60 days'),
      ('alice-dev', 82.0, NOW() - INTERVAL '30 days'),
      ('alice-dev', 88.5, NOW()),
      ('john-dev', 60.0, NOW() - INTERVAL '90 days'),
      ('john-dev', 65.3, NOW() - INTERVAL '60 days'),
      ('john-dev', 70.8, NOW() - INTERVAL '30 days'),
      ('john-dev', 75.2, NOW()),
      ('rahul-dev', 68.0, NOW() - INTERVAL '90 days'),
      ('rahul-dev', 73.5, NOW() - INTERVAL '60 days'),
      ('rahul-dev', 78.2, NOW() - INTERVAL '30 days'),
      ('rahul-dev', 81.0, NOW())
    ) AS t(login, score, ts)
    WHERE c.login = t.login;

    -- Demo issues
    INSERT INTO issues (github_issue_id, number, title, body, author_login, state, labels, html_url, repository_id, github_created_at)
    SELECT
      unnest(ARRAY[201,202,203,204,205]),
      unnest(ARRAY[28,27,26,25,24]),
      unnest(ARRAY[
        'Login button does not work on mobile',
        'Login button not working',
        'Dark mode crashes the application',
        'API rate limiting not properly handled',
        'Database connection drops under load'
      ]),
      unnest(ARRAY[
        'When tapping the login button on mobile devices, nothing happens.',
        'The login button appears to be broken. Clicking it does nothing.',
        'When dark mode is enabled and you navigate to settings, the app crashes.',
        'GitHub API rate limits are not handled gracefully in the backend.',
        'Under high load, database connections are being exhausted.'
      ]),
      unnest(ARRAY['alice-dev','john-dev','rahul-dev','alice-dev','john-dev']),
      unnest(ARRAY['open','open','open','open','closed']),
      unnest(ARRAY['["bug","mobile"]','["bug"]','["bug","ui"]','["enhancement","backend"]','["bug","database"]']::JSONB[]),
      unnest(ARRAY[
        'https://github.com/demo-org/sentrygate-demo/issues/28',
        'https://github.com/demo-org/sentrygate-demo/issues/27',
        'https://github.com/demo-org/sentrygate-demo/issues/26',
        'https://github.com/demo-org/sentrygate-demo/issues/25',
        'https://github.com/demo-org/sentrygate-demo/issues/24'
      ]),
      (SELECT id FROM repositories WHERE github_repo_id = 101),
      NOW() - INTERVAL '5 days'
    ;

    -- Issue similarities (issues 28 and 27 are similar)
    INSERT INTO issue_similarities (source_issue_id, target_issue_id, similarity_score, method)
    SELECT i1.id, i2.id, 0.89, 'COSINE_TF_IDF'
    FROM issues i1, issues i2
    WHERE i1.number = 28 AND i2.number = 27;

    -- Update duplicate status
    UPDATE issues SET duplicate_status = 'POTENTIAL_DUPLICATE'
    WHERE number IN (27, 28) AND repository_id = (SELECT id FROM repositories WHERE github_repo_id = 101);

    -- Demo notifications
    INSERT INTO notifications (user_id, type, title, message, is_read, repository_id)
    SELECT u.id, n.type, n.title, n.message, n.is_read, r.id
    FROM users u, repositories r,
    (VALUES
      ('PR_VALIDATION_FAILED', 'PR #39 Validation Failed', 'Pull request "Add payment module integration" failed validation with 3 critical issues.', false),
      ('PR_VALIDATION_PASSED', 'PR #42 Validation Passed', 'Pull request "feat(auth): Add GitHub OAuth authentication" passed all validation checks.', false),
      ('DUPLICATE_ISSUE_DETECTED', 'Potential Duplicate Issue', 'Issue #27 may be a duplicate of Issue #28 (89% similarity). Please review.', true),
      ('REPOSITORY_CONNECTED', 'Repository Connected', 'Repository sentrygate-demo has been successfully connected and monitoring activated.', true),
      ('WEBHOOK_RECEIVED', 'Webhook Processed', 'Successfully processed pull_request event for sentrygate-demo.', true)
    ) AS n(type, title, message, is_read)
    WHERE u.login = 'demo-admin' AND r.github_repo_id = 101;

    -- Demo activity logs
    INSERT INTO activity_logs (actor_login, actor_user_id, event_type, repository_id, entity_type, description)
    SELECT 'demo-admin', u.id, e.event_type, r.id, e.entity_type, e.description
    FROM users u, repositories r,
    (VALUES
      ('REPOSITORY_CONNECTED', 'REPOSITORY', 'Connected repository sentrygate-demo to SentryGate monitoring'),
      ('WEBHOOK_RECEIVED', 'WEBHOOK', 'Processed pull_request.opened webhook event for PR #42'),
      ('VALIDATION_COMPLETED', 'PULL_REQUEST', 'PR #42 validation completed: PASSED (7 pass, 0 fail, 1 warning)'),
      ('VALIDATION_COMPLETED', 'PULL_REQUEST', 'PR #41 validation completed: PASSED (8 pass, 0 fail, 0 warning)'),
      ('VALIDATION_COMPLETED', 'PULL_REQUEST', 'PR #39 validation failed: FAILED (3 pass, 3 fail, 1 warning)'),
      ('RULE_UPDATED', 'RULE', 'Repository rules updated for sentrygate-demo'),
      ('DUPLICATE_DETECTED', 'ISSUE', 'Potential duplicate detected: Issue #27 similar to Issue #28 (89%)'),
      ('TRUST_SCORE_UPDATED', 'CONTRIBUTOR', 'Trust score recalculated for alice-dev: 88.5/100')
    ) AS e(event_type, entity_type, description)
    WHERE u.login = 'demo-admin' AND r.github_repo_id = 101;

  END IF;
END $$;
