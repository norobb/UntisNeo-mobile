const fs = require('fs');

function fixNesting(filePath) {
    let content = fs.readFileSync(filePath, 'utf-8');
    
    // StringResources.get(StringResources.get("...")) -> StringResources.get("...")
    // We will do a generic regex replace inside
    content = content.replace(/StringResources\.get\(\s*StringResources\.get\(\s*"([^"]+)"\s*\)\s*\)/g, 'StringResources.get("$1")');
    // Just in case it's nested more times
    content = content.replace(/StringResources\.get\(\s*StringResources\.get\(\s*"([^"]+)"\s*\)\s*\)/g, 'StringResources.get("$1")');
    content = content.replace(/StringResources\.get\(\s*StringResources\.get\(\s*"([^"]+)"\s*\)\s*\)/g, 'StringResources.get("$1")');
    
    fs.writeFileSync(filePath, content);
}

fixNesting('app/src/main/java/com/example/ui/screens/AllScreens.kt');
fixNesting('app/src/main/java/com/example/MainActivity.kt');
