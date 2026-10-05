'use strict';

let raw = '';

process.stdin.setEncoding('utf8');

process.stdin.on('data', function (chunk) {
  raw += chunk;
});

process.stdin.on('end', function () {
  let input;

  try {
    input = JSON.parse(raw);
  } catch (e) {
    process.stderr.write('Invalid hook input\n');
    process.exit(2);
  }

  const toolInput = input && input.tool_input;

  if (!toolInput || typeof toolInput.command !== 'string') {
    process.exit(0);
  }

  const command = toolInput.command;

  if (/\bgit(\.exe)?\s+push\b/i.test(command)) {
    process.stdout.write(JSON.stringify({
      hookSpecificOutput: {
        hookEventName: 'PreToolUse',
        permissionDecision: 'deny',
        permissionDecisionReason:
          'Blocked by project policy: git push is not allowed.'
      }
    }));

    process.exit(0);
  }

  process.exit(0);
});